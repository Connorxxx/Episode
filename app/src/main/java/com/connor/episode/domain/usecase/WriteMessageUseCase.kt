package com.connor.episode.domain.usecase

import arrow.core.Either
import arrow.core.left
import com.connor.episode.core.utils.hexStringToByteArray
import com.connor.episode.domain.model.business.Message
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.PreferencesRepository
import com.connor.episode.domain.repository.SerialPortRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class WriteMessageUseCase @Inject constructor(
    private val serialPortRepository: SerialPortRepository,
    private val messageRepository: MessageRepository,
    private val preferencesRepository: PreferencesRepository
) {

    suspend operator fun invoke(msg: String): Either<String, Unit> {
        if (msg.isEmpty()) return "Message can not be empty".left()
        val sendFormat = preferencesRepository.getSerialPref().settings.sendFormat
        val bytesMsg =
            if (sendFormat == 0) msg.hexStringToByteArray()
            else msg.toByteArray(Charsets.US_ASCII)
        messageRepository.addMessage(Message(msg, true))
        return serialPortRepository.write(bytesMsg).mapLeft { it.msg }
    }
}