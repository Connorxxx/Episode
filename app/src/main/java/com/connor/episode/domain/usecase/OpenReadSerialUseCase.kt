package com.connor.episode.domain.usecase

import arrow.core.left
import arrow.core.right
import com.connor.episode.domain.model.error.UiError
import com.connor.episode.domain.model.error.SerialPortError
import com.connor.episode.domain.model.config.SerialConfig
import com.connor.episode.domain.repository.PreferencesRepository
import com.connor.episode.domain.repository.SerialPortRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

@ViewModelScoped
class OpenReadSerialUseCase @Inject constructor(
    private val serialPortRepository: SerialPortRepository,
    private val preferencesRepository: PreferencesRepository
) {

    operator fun invoke(config: SerialConfig.() -> Unit) = flow {
        val cf = SerialConfig().apply(config)
        if (cf.devicePath.isEmpty() || cf.baudRate.isEmpty()) emit(UiError(msg = "Device path or baud rate is empty").left())
        preferencesRepository.updateSerialPref {
            it.copy(
                serialPort = cf.devicePath,
                baudRate = cf.baudRate
            )
        }
        serialPortRepository.openAndRead {
            devicePath = cf.devicePath
            baudRate = cf.baudRate
        }.transform {
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
                    emit(bytes.right())
                }
            )
        }.also { emitAll(it) }
    }
}