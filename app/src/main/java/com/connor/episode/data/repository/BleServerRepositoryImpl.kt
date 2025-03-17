package com.connor.episode.data.repository

import arrow.core.left
import com.connor.episode.core.utils.getBytesMsg
import com.connor.episode.core.utils.withTimeout
import com.connor.episode.data.remote.ble.BleServer
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.msgType
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.model.error.BleError
import com.connor.episode.domain.repository.BleServerRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.timeout
import javax.inject.Inject
import kotlin.time.Duration

class BleServerRepositoryImpl @Inject constructor(
    private val bleServer: BleServer
) : BleServerRepository {

    override fun startAdvertising(timeout: Duration) =
        bleServer.startAdvertising
            .withTimeout(timeout)
            .catch {
                if (it is TimeoutCancellationException) emit(
                    BleError.AdvertiseTimeout("Advertise timeout").left()
                )
            }

    @OptIn(ExperimentalStdlibApi::class)
    override fun startServerAndRead(typeProvider: suspend (Owner) -> Int) = bleServer.startServerAndRead.map {
        it.map { (ip, bytes) ->
            val currentType = typeProvider(Owner.BLE)
            val content =
                if (currentType == 0) bytes.toHexString().uppercase() else bytes.decodeToString()
            MessageEntity(
                name = ip,
                content = content,
                isMe = false,
                type = msgType[currentType],
                owner = Owner.BLE
            )
        }
    }

    override suspend fun sendMessage(msg: String, msgType: Int) = bleServer.sendMessage(getBytesMsg(msgType, msg))

}