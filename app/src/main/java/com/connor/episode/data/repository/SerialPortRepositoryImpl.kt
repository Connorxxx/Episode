package com.connor.episode.data.repository

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.none
import arrow.core.raise.either
import arrow.core.some
import com.connor.episode.core.utils.getBytesMsg
import com.connor.episode.data.remote.serial.SerialPortSource
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.SerialPortDevice
import com.connor.episode.domain.model.config.SerialConfig
import com.connor.episode.domain.model.error.SerialPortError
import com.connor.episode.domain.repository.SerialPortRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import serialport_api.SerialPort
import javax.inject.Inject

class SerialPortRepositoryImpl @Inject constructor(
    private val serialPortSource: SerialPortSource,
) : SerialPortRepository {

    private var serialPort: Option<SerialPort> = none()

    override val getAllDevices
        get() = serialPortSource.getAllDevicesPath.sortedWith(compareBy({ it.length }, { it }))
            .map {
                SerialPortDevice(name = it.substringAfterLast("/"), path = it)
            }

    @OptIn(ExperimentalStdlibApi::class)
    override fun openAndRead(config: SerialConfig, typeProvider: suspend (Owner) -> Int) = flow {
        serialPortSource.open(config.devicePath, config.baudRate.toInt()).map {
            serialPort = it.some()
            serialPortSource.read(it).map {
                it.map { bytes ->
                    val currentType = typeProvider(Owner.SerialPort)
                    if (currentType == 0) bytes.toHexString()
                        .uppercase() else bytes.decodeToString()
                }
            }
        }.fold(
            ifLeft = { emit(it.left()) },
            ifRight = {
                emitAll(it)
            }
        )
    }

    override suspend fun sendMessage(data: String, msgType: Int) = withContext(Dispatchers.IO) {
        either {
            val bytesMsg = getBytesMsg(msgType, data)
            val serial =
                serialPort.getOrElse { raise(SerialPortError.Open("Serial port not opened")) }
            serialPortSource.write(serial, bytesMsg).bind()
        }
    }

    override suspend fun close() {
        withContext(Dispatchers.IO) {
            serialPort.onSome {
                it.close()
                serialPort = none()
            }
        }
    }
}