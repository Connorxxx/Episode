package com.connor.episode.domain.usecase

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.connor.episode.data.mapper.toMessage
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.PreferencesRepository
import com.connor.episode.domain.repository.SerialPortRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class ResendUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val preferencesRepository: PreferencesRepository,
    private val serialPortRepository: SerialPortRepository,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(resend: Boolean) = flow {
        preferencesRepository.updateSerialPref {
            it.copy(settings = it.settings.copy(resend = resend))
        }
        if (!resend) return@flow
        val lastMsg = messageRepository.getLastSendMessage() ?: run {
            emit("No message to resend".left())
            return@flow
        }
        preferencesRepository.prefFlow.transformLatest { pref ->
                while (true) {
                    delay(pref.settings.resendSeconds.seconds)
                    emit(Unit)
                }
            }.onStart { emit(Unit) }
            .transformLatest {
                serialPortRepository.write(lastMsg.bytes).fold(
                    ifLeft = { emit(it.msg.left()) },
                    ifRight = {
                        messageRepository.addMessage(lastMsg.copy(id = 0))
                        emit(lastMsg.toMessage().right())
                    }
                )
            }.collect { emit(it) }
    }
}