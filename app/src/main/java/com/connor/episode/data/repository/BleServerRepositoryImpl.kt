package com.connor.episode.data.repository

import arrow.core.left
import com.connor.episode.core.utils.getBytesMsg
import com.connor.episode.data.remote.ble.BleServer
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

    @OptIn(FlowPreview::class)
    override fun startAdvertising(timeout: Duration) =
        bleServer.startAdvertising
            .timeout(timeout)
            .catch {
                if (it is TimeoutCancellationException) emit(
                    BleError.AdvertiseTimeout("Advertise timeout").left()
                )
            }

    override val startServerAndRead = bleServer.startServerAndRead.map { it.map { it.decodeToString() } }

    override suspend fun sendMessage(data: String, msgType: Int) = bleServer.sendMessage(getBytesMsg(msgType, data))

}