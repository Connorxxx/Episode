package com.connor.episode.domain.repository

import arrow.core.Either
import com.connor.episode.domain.model.error.BleError
import com.connor.episode.domain.repository.common.SendMessage
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface BleServerRepository : SendMessage<BleError> {

    fun startAdvertising(timeout: Duration): Flow<Either<BleError, Unit>>

    /**
     * base on coroutine, cancel flow to stop server
     */
    val startServerAndRead: Flow<Either<BleError, String>>

    suspend fun sendBroadcastMessage(data: String, msgType: Int): Either<BleError, Unit> = sendMessage(data, msgType)

}
