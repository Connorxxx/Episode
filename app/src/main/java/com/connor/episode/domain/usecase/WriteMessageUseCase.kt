package com.connor.episode.domain.usecase

import com.connor.episode.core.utils.getBytesMsg
import com.connor.episode.core.utils.hexStringToByteArray
import com.connor.episode.core.utils.logCat
import com.connor.episode.data.mapper.toEntity
import com.connor.episode.domain.model.business.Message
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.msgType
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class WriteMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val preferencesRepository: PreferencesRepository,
) {

    suspend operator fun invoke(msg: String, owner: Owner): ByteArray {
        if (msg.isEmpty()) Error("Message can not be empty")
        val type = when (owner) {
            Owner.SerialPort -> preferencesRepository.serialPrefFlow.first().settings.sendFormat
            Owner.TCP -> preferencesRepository.tcpPrefFlow.first().settings.sendFormat
            Owner.UDP -> preferencesRepository.udpPrefFlow.first().settings.sendFormat
            Owner.WebSocket -> preferencesRepository.webSocketPrefFlow.first().settings.sendFormat
        }
        val bytesMsg = getBytesMsg(type, msg)
        val message = Message(
            0,
            "Client",
            msg,
            true,
            type = msgType[type],
            owner = owner
        )
        val xx = message.toEntity().copy(bytes = bytesMsg)
        "WriteMessageUseCase: $xx".logCat()
        messageRepository.addMessage(xx)
        return bytesMsg
    }

}