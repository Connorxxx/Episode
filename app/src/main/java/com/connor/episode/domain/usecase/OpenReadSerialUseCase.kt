package com.connor.episode.domain.usecase

import arrow.core.left
import arrow.core.right
import com.connor.episode.core.utils.logCat
import com.connor.episode.data.mapper.toMessage
import com.connor.episode.domain.model.error.UiError
import com.connor.episode.domain.model.error.SerialPortError
import com.connor.episode.domain.model.config.SerialConfig
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.model.uimodel.SerialPortUi
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.PreferencesRepository
import com.connor.episode.domain.repository.SerialPortRepository
import javax.inject.Singleton
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

@Singleton
class OpenReadSerialUseCase @Inject constructor(
    private val serialPortRepository: SerialPortRepository,
    private val preferencesRepository: PreferencesRepository,
    private val messageRepository: MessageRepository
) {

    @OptIn(ExperimentalStdlibApi::class)
    operator fun invoke(config: SerialConfig.() -> Unit) = flow {
        val cf = SerialConfig().apply(config)
        if (cf.devicePath.isEmpty() || cf.baudRate.isEmpty()) emit(UiError(msg = "Device path or baud rate is empty").left())
        preferencesRepository.updateSerialPref {
            it.copy(
                serialPort = cf.devicePath.substringAfterLast("/"),
                baudRate = cf.baudRate
            )
        }
        serialPortRepository.openAndRead(cf).transform {
            it.fold(
                ifLeft = { err ->
                    when (err) {
                        is SerialPortError.Open,
                        is SerialPortError.Read.EndOfStream,
                        is SerialPortError.Read.IO -> {
                            emit(UiError(msg = err.msg, isFatal = true).left())
                            serialPortRepository.close()
                            currentCoroutineContext().cancel()
                        }

                        else -> emit(UiError(msg = err.msg).left())
                    }
                },
                ifRight = { bytes ->
                    "Received ${bytes.size} bytes".logCat()
                    val message = MessageEntity(
                        content = bytes.toHexString(),
                        bytes = bytes,
                        isMe = false,
                        type = SerialPortUi().options[preferencesRepository.getSerialPref().settings.receiveFormat]
                    )
                    messageRepository.addMessage(message)
                    emit(message.toMessage().right())
                }
            )
        }.also { emitAll(it) }
    }
}