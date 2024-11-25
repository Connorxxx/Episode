package com.connor.episode.domain.usecase

import arrow.core.left
import arrow.core.right
import com.connor.episode.domain.error.Error
import com.connor.episode.domain.error.SerialPortError
import com.connor.episode.domain.model.SerialPortModel
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

    operator fun invoke(model: SerialPortModel, ) = flow {
        val path =
            model.serialPorts.find { it.contains(model.serialPort) }.takeIf { it != null } ?: run {
                emit(Error(msg = "Device not found", isFatal = true).left())
                return@flow
            }
        preferencesRepository.updateSerialPref(
            model.copy(
                serialPort = model.serialPort,
                baudRate = model.baudRate
            )
        )
        serialPortRepository.openAndRead {
            devicePath = path
            baudRate = model.baudRate
        }.transform {
            it.fold(
                ifLeft = { err ->
                    when (err) {
                        is SerialPortError.Open,
                        is SerialPortError.Read.EndOfStream,
                        is SerialPortError.Read.IO -> {
                            emit(Error(msg = err.msg, isFatal = true).left())
                            serialPortRepository.close()
                            currentCoroutineContext().cancel()
                        }

                        else -> emit(Error(msg = err.msg).left())
                    }
                },
                ifRight = { bytes ->
                    emit(bytes.right())
                }
            )
        }.also { emitAll(it) }
    }
}