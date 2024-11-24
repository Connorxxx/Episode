package com.connor.episode.data.repository

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.none
import arrow.core.raise.either
import arrow.core.some
import com.connor.episode.BuildConfig
import com.connor.episode.data.local.database.dao.MessageDao
import com.connor.episode.data.local.database.entity.MessageEntity
import com.connor.episode.data.local.datastore.PreferencesModule
import com.connor.episode.data.mapper.toMessage
import com.connor.episode.data.mapper.toModel
import com.connor.episode.data.mapper.toPreferences
import com.connor.episode.data.remote.serial.SerialPortSource
import com.connor.episode.domain.error.SerialPortError
import com.connor.episode.domain.model.SerialPortModel
import com.connor.episode.domain.repository.SerialPortRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import serialport_api.SerialPort

class SerialPortRepositoryImpl @Inject constructor(
    private val serialPortSource: SerialPortSource,
    private val preferencesModule: PreferencesModule,
    private val messageDao: MessageDao
) : SerialPortRepository {

    private var serialPort: Option<SerialPort> = none()

    private val getAllDevices get() = serialPortSource.getAllDevices.sortedWith(compareBy({ it.length }, { it }))

    private val getAllDevicesPath get() = serialPortSource.getAllDevicesPath.sortedWith(compareBy({ it.length }, { it }))

    private val serialPref = preferencesModule.serialPref

    override suspend fun getSerialPortModel(): SerialPortModel {
        val list = if (BuildConfig.DEBUG) listOf("ttyS0", "ttyS1", "ttyS2", "ttyS3") else getAllDevicesPath
        val serialPref = preferencesModule.serialPref.data.first()
        val messages = messageDao.getAllMessages().first().map { it.toMessage() }
        return SerialPortModel(
            serialPorts = list,
            serialPort = serialPref.serialPort,
            baudRate = serialPref.baudRate,
            messages = messages,
            resend = serialPref.resend,
            resendSeconds = serialPref.resendSeconds,
            sendFormat = serialPref.sendFormat,
            receiveFormat = serialPref.receiveFormat
        )
    }

    override suspend fun updatePreferences(model: SerialPortModel) =
        serialPref.updateData { model.toPreferences() }.toModel()


    override suspend fun addMessage(message: String, isMe: Boolean) {
        messageDao.insertMessage(MessageEntity(content = message, isMe = isMe))
    }

    override suspend fun deleteAllMessages() {
        messageDao.deleteAllMessages()
    }

    override fun openAndRead(path: String, baudRate: Int) = flow {
        serialPortSource.open(path, baudRate).map {
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