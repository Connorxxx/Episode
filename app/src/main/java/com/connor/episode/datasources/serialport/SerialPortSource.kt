package com.connor.episode.datasources.serialport

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.connor.episode.errors.SerialPortError
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import serialport_api.SerialPort
import serialport_api.SerialPortFinder
import java.io.File

class SerialPortSource @Inject constructor() {

    val getAllDevices: Array<String> get() = SerialPortFinder().allDevices

    val getAllDevicesPath: Array<String> get() = SerialPortFinder().allDevices

    fun open(path: String, baudRate: Int) = Either.catch {
        SerialPort(File(path), baudRate, 0)
    }.mapLeft { SerialPortError.Open(it.message ?: "Serial Port Open Error") }

    fun write(serialPort: SerialPort, data: ByteArray) = Either.catch {
        serialPort.outputStream.write(data)
    }.mapLeft { SerialPortError.Write(it.message ?: "Serial Port Write Error") }

    fun read(serialPort: SerialPort) = flow {
        val buffer = ByteArray(256)
        while (currentCoroutineContext().isActive) {
            val bytesRead = Either.catch { serialPort.inputStream.read(buffer) }
                .mapLeft { SerialPortError.Read(it.message ?: "Serial Port Read Error") }
                .flatMap {
                    if (it > 0) it.right() else SerialPortError.Read("byte size <= 0").left()
                }
            val e = bytesRead.map { buffer.copyOfRange(0, it) }
            emit(e)
            if (e.isLeft()) break
        }
    }.flowOn(Dispatchers.IO)
}