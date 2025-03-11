package com.connor.episode.domain.usecase

import com.connor.episode.core.utils.logCat
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.msgType
import com.connor.episode.domain.model.config.SerialConfig
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.model.error.SerialPortError
import com.connor.episode.domain.model.error.UiError
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.PreferencesRepository
import com.connor.episode.domain.repository.SerialPortRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

class OpenReadSerialUseCase @Inject constructor(
    private val serialPortRepository: SerialPortRepository,
    private val preferencesRepository: PreferencesRepository,
    private val messageRepository: MessageRepository
) {

    @OptIn(ExperimentalStdlibApi::class)
    operator fun invoke(config: SerialConfig.() -> Unit) = flow {
        val cf = SerialConfig().apply(config)
        if (cf.devicePath.isEmpty() || cf.baudRate.isEmpty()) emit(UiError(msg = "Device path or baud rate is empty"))
        preferencesRepository.updateSerialPref {
            it.copy(
                serialPort = cf.devicePath.substringAfterLast("/"),
                baudRate = cf.baudRate
            )
        }
        serialPortRepository.openAndRead(cf).map {
            it.onRight { bytes ->
                val type = preferencesRepository.serialPrefFlow.first().settings.receiveFormat
                "Received ${bytes.size} bytes  type: $type".logCat()
                val content = if (type == 0) bytes.toHexString().uppercase() else bytes.decodeToString()
                val message = MessageEntity(
                    name = "Server",
                    content = content,
                    bytes = bytes,
                    isMe = false,
                    type = msgType[type],
                    owner = Owner.SerialPort
                )
                messageRepository.addMessage(message)
            }.mapLeft { err ->
                when (err) {
                    is SerialPortError.Open,
                    is SerialPortError.Read.EndOfStream,
                    is SerialPortError.Read.IO -> {
                        serialPortRepository.close()
                        currentCoroutineContext().cancel()
                        UiError(msg = err.msg, isFatal = true)
                    }
                    else -> UiError(msg = err.msg)
                }
            }
        }.also { emitAll(it) }
    }.flowOn(Dispatchers.IO)
}