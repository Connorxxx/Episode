package com.connor.episode.domain.repository

import android.bluetooth.BluetoothDevice
import arrow.core.Either
import com.connor.episode.domain.model.error.BleError
import com.connor.episode.domain.repository.common.SendMessage
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface BleClientRepository : SendMessage<BleError> {

    fun scanDevice(timeout: Duration): Flow<Either<BleError, BluetoothDevice>>

    fun connectAndRead(device: BluetoothDevice): Flow<Either<BleError, String>>

    suspend fun sendMessageToServer(data: String, msgType: Int): Either<BleError, Unit> = sendMessage(data, msgType)
}