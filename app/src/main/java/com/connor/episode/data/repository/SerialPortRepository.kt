package com.connor.episode.data.repository

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.none
import arrow.core.raise.either
import arrow.core.some
import com.connor.episode.data.remote.serial.SerialPortSource
import com.connor.episode.domain.error.SerialPortError
import jakarta.inject.Inject
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import serialport_api.SerialPort

class SerialPortRepository @Inject constructor(
    private val serialPortSource: SerialPortSource
) {

    private var serialPort: Option<SerialPort> = none()

    val getAllDevices get() = serialPortSource.getAllDevices

    val getAllDevicesPath get() = serialPortSource.getAllDevicesPath

    fun openAndRead(path: String, baudRate: Int) = flow {
        serialPortSource.open(path, baudRate).map {
            serialPort = it.some()
            serialPortSource.read(it)
        }.fold(
            ifLeft = { emit(it.left()) },
            ifRight = { emitAll(it) }
        )
    }

    fun write(data: ByteArray) = either {
        val serial = serialPort.getOrElse { raise(SerialPortError.Open("Serial port not opened")) }
        serialPortSource.write(serial, data).bind()
    }

    fun close() {
        serialPort.onSome {
            it.close()
            serialPort = none()
        }
    }
}