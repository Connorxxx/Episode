package com.connor.episode.data.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import arrow.core.left
import com.connor.episode.core.utils.getBytesMsg
import com.connor.episode.core.utils.withTimeout
import com.connor.episode.data.remote.ble.BleClient
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.msgType
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.model.error.BleError
import com.connor.episode.domain.repository.BleClientRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Duration

class BleClientRepositoryImpl @Inject constructor(
    private val bleClient: BleClient
) : BleClientRepository {

    override fun scanDevice(timeout: Duration) = bleClient.scanDevice.withTimeout(timeout).catch {
        if (it is TimeoutCancellationException) emit(BleError.ScanTimeout("Scan timeout").left())
    }


    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalStdlibApi::class)
    override fun connectAndRead(device: BluetoothDevice, typeProvider: suspend (Owner) -> Int) = bleClient.connect(device).map {
        it.map { bytes ->
            val currentType = typeProvider(Owner.BLE)
            val content =
                if (currentType == 0) bytes.toHexString().uppercase() else bytes.decodeToString()
            MessageEntity(
                name = device.name,
                content = content,
                isMe = false,
                type = msgType[currentType],
                owner = Owner.BLE
            )
        }
    }

    override suspend fun sendMessage(msg: String, msgType: Int) = bleClient.sendMessage(getBytesMsg(msgType, msg))

}