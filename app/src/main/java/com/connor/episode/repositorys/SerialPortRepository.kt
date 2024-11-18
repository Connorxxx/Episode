package com.connor.episode.repositorys

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import com.connor.episode.datasources.serialport.SerialPortSource
import com.connor.episode.errors.SerialPortError
import jakarta.inject.Inject
import serialport_api.SerialPort

class SerialPortRepository @Inject constructor(
    private val serialPortSource: SerialPortSource
) {

    private var serialPort: Option<SerialPort> = none()

    val getAllDevices get() = serialPortSource.getAllDevices

    val getAllDevicesPath get() = serialPortSource.getAllDevicesPath

    fun open(path: String, baudRate: Int) = serialPortSource.open(path, baudRate).map {
        serialPort = it.some()
        serialPortSource.read(it)
    }

    fun write(data: ByteArray) {
        serialPort.onSome { serialPortSource.write(it, data) }
    }


    fun close() {
        serialPort.onSome {
            it.close()
            serialPort = none()
        }
    }

    private fun getSerialPort() = serialPort.toEither {
        SerialPortError.None("Serial Port Not Open, State: ${serialPort.isSome()}")
    }
}