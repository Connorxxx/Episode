package com.connor.episode.data.repository

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.none
import arrow.core.raise.either
import arrow.core.some
import com.connor.episode.data.remote.serial.SerialPortSource
import com.connor.episode.domain.config.SerialConfig
import com.connor.episode.domain.error.SerialPortError
import com.connor.episode.domain.repository.SerialPortRepository
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import serialport_api.SerialPort
import javax.inject.Inject

class SerialPortRepositoryImpl @Inject constructor(
    private val serialPortSource: SerialPortSource,
) : SerialPortRepository {

    private var serialPort: Option<SerialPort> = none()

    override val getAllDevices
        get() = serialPortSource.getAllDevices.sortedWith(compareBy({ it.length }, { it }))

    override fun openAndRead(config: SerialConfig.() -> Unit) = flow {
        val cf = SerialConfig().apply(config)
        serialPortSource.open(cf.devicePath, cf.baudRate.toInt()).map {
            serialPort = it.some()
            serialPortSource.read(it)
        }.fold(
            ifLeft = { emit(it.left()) },
            ifRight = { emitAll(it) }
        )
    }

    override fun write(data: ByteArray) = either {
        val serial = serialPort.getOrElse { raise(SerialPortError.Open("Serial port not opened")) }
        serialPortSource.write(serial, data).bind()
    }

    override fun close() {
        serialPort.onSome {
            it.close()
            serialPort = none()
        }
    }
}