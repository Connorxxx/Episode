package com.connor.episode.domain.usecase

import android.bluetooth.BluetoothDevice
import com.connor.episode.domain.model.error.UiError
import com.connor.episode.domain.repository.BleClientRepository
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BleClientConnectUseCase @Inject constructor(
    private val bleClientRepository: BleClientRepository,
    private val messageRepository: MessageRepository,
    private val preferencesRepository: PreferencesRepository
) {

    operator fun invoke(device: BluetoothDevice) = bleClientRepository.connectAndRead(device, preferencesRepository::getReceiveFormat).map {
        it.onRight { message ->
            messageRepository.addMessage(message)
        }.mapLeft { err ->
            UiError(msg = err.msg, isFatal = true)
        }.leftOrNull()
    }.filterNotNull()
}