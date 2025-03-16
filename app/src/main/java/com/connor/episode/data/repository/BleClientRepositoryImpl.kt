package com.connor.episode.data.repository

import android.bluetooth.BluetoothDevice
import arrow.core.left
import com.connor.episode.core.utils.getBytesMsg
import com.connor.episode.data.remote.ble.BleClient
import com.connor.episode.domain.model.error.BleError
import com.connor.episode.domain.repository.BleClientRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.timeout
import javax.inject.Inject
import kotlin.time.Duration

class BleClientRepositoryImpl @Inject constructor(
    private val bleClient: BleClient
) : BleClientRepository {

    @OptIn(FlowPreview::class)
    override fun scanDevice(timeout: Duration) = bleClient.scanDevice.timeout(timeout).catch {
        if (it is TimeoutCancellationException) emit(BleError.ScanTimeout("Scan timeout").left())
    }

    override fun connectAndRead(device: BluetoothDevice) = bleClient.connect(device).map { it.map { it.decodeToString() } }

    override suspend fun sendMessage(data: String, msgType: Int) = bleClient.sendMessage(getBytesMsg(msgType, data))

}