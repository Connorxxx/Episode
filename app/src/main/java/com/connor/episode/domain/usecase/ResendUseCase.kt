package com.connor.episode.domain.usecase

import arrow.core.left
import arrow.core.right
import com.connor.episode.data.mapper.toMessage
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.PreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class ResendUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val preferencesRepository: PreferencesRepository
) {

    operator fun invoke(resend: Boolean, time: Int) = flow {
        preferencesRepository.updateSerialPref {
            it.copy(settings = it.settings.copy(resend = resend))
        }
        if (!resend) return@flow
        val lastMsg = messageRepository.getLastMessage()
        if (lastMsg == null) {
            emit("No message to resend".left())
            return@flow
        }
        while (true) {
            messageRepository.addMessage(lastMsg)
            emit(lastMsg.toMessage().right())
            delay(time.seconds)
        }
    }
}