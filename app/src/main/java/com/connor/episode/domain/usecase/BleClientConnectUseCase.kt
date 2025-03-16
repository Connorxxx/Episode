package com.connor.episode.domain.usecase

import android.bluetooth.BluetoothDevice
import com.connor.episode.domain.repository.BleClientRepository
import javax.inject.Inject

class BleClientConnectUseCase @Inject constructor(
    private val bleClientRepository: BleClientRepository
) {

    operator fun invoke(device: BluetoothDevice) = bleClientRepository.connectAndRead(device)
}