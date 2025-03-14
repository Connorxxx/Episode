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
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WriteMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val preferencesRepository: PreferencesRepository,
) {

    suspend operator fun invoke(msg: String, owner: Owner): ByteArray {
        if (msg.isEmpty()) Error("Message can not be empty")
        val type = when (owner) {
            Owner.SerialPort -> preferencesRepository.serialPrefFlow.map { it.settings }
            Owner.TCP -> preferencesRepository.tcpPrefFlow.map { it.settings }
            Owner.UDP -> preferencesRepository.udpPrefFlow.map { it.settings }
            Owner.WebSocket -> preferencesRepository.webSocketPrefFlow.map { it.settings }
        }.first().sendFormat
        val bytesMsg = getBytesMsg(type, msg)
        val message = Message(
            0,
            "Client",
            msg,
            true,
            type = msgType[type],
            owner = owner
        )
        messageRepository.addMessage(message.toEntity().copy(bytes = bytesMsg))
        return bytesMsg
    }

}