package com.connor.episode.data.remote.serial

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.connor.episode.domain.error.SerialPortError
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

    val getAllDevicesPath: Array<String> get() = SerialPortFinder().allDevicesPath

    fun open(path: String, baudRate: Int) = Either.Companion.catch {
        SerialPort(File(path), baudRate, 0)
    }.mapLeft { SerialPortError.Open(it.message ?: "Open Error") }

    fun write(serialPort: SerialPort, data: ByteArray) = Either.Companion.catch {
        serialPort.outputStream.write(data)
    }.mapLeft { SerialPortError.Write(it.message ?: "Write Error") }

    fun read(serialPort: SerialPort) = flow {
        val buffer = ByteArray(256)
        while (currentCoroutineContext().isActive) {
            val bytesRead = Either.Companion.catch { serialPort.inputStream.read(buffer) }
                .mapLeft { SerialPortError.Read.IO(it.message ?: "Read Error") }
                .flatMap {
                    when (it) {
                        -1 -> SerialPortError.Read.EndOfStream().left()
                        0 -> SerialPortError.Read.NoData().left()
                        else -> it.right()
                    }
                }
            val e = bytesRead.map { buffer.copyOfRange(0, it) }
            emit(e)
        }
    }.flowOn(Dispatchers.IO)
}