package com.connor.episode.domain.usecase

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.connor.episode.core.utils.hexStringToByteArray
import com.connor.episode.data.mapper.toEntity
import com.connor.episode.domain.model.business.Message
import com.connor.episode.domain.model.business.SerialPortModel
import com.connor.episode.domain.model.uimodel.SerialPortUi
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.PreferencesRepository
import com.connor.episode.domain.repository.SerialPortRepository
import javax.inject.Singleton
import javax.inject.Inject

@Singleton
class WriteMessageUseCase @Inject constructor(
    private val serialPortRepository: SerialPortRepository,
    private val messageRepository: MessageRepository,
    private val preferencesRepository: PreferencesRepository
) {

    suspend operator fun invoke(msg: String): Either<String, Message> {
        if (msg.isEmpty()) return "Message can not be empty".left()
        val settings = preferencesRepository.getSerialPref().settings
        val type = settings.sendFormat
        val bytesMsg = if (type == 0) msg.hexStringToByteArray()
            else msg.toByteArray(Charsets.US_ASCII)
        val message = Message(msg, true, type = SerialPortUi().options[type])
        messageRepository.addMessage(message.toEntity().copy(bytes = bytesMsg))
        return serialPortRepository.write(bytesMsg).fold(
            ifLeft = { it.msg.left() },
            ifRight = { message.right() }
        )
    }
}