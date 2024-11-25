package com.connor.episode.domain.usecase

import com.connor.episode.domain.model.SerialPortModel
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.PreferencesRepository
import com.connor.episode.domain.repository.SerialPortRepository
import javax.inject.Inject

class GetSerialModelUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val serialPortRepository: SerialPortRepository,
    private val messageRepository: MessageRepository
) {

    suspend operator fun invoke(): SerialPortModel {
        val list = serialPortRepository.getAllDevices
        val serialPref = preferencesRepository.getSerialPref()
        val messages = messageRepository.getAllMessages()
        return SerialPortModel(
            serialPorts = list,
            serialPort = serialPref.serialPort,
            baudRate = serialPref.baudRate,
            messages = messages,
            resend = serialPref.resend,
            resendSeconds = serialPref.resendSeconds,
            sendFormat = serialPref.sendFormat,
            receiveFormat = serialPref.receiveFormat,
        )
    }
}