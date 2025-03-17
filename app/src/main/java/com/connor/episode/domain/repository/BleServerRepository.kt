package com.connor.episode.domain.repository

import arrow.core.Either
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.model.error.BleError
import com.connor.episode.domain.repository.common.SendMessage
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface BleServerRepository : SendMessage<BleError> {

    fun startAdvertising(timeout: Duration): Flow<Either<BleError, Unit>>

    /**
     * base on coroutine, cancel flow to stop server
     */
    fun startServerAndRead(typeProvider: suspend (Owner) -> Int): Flow<Either<BleError, MessageEntity>>

    suspend fun sendBroadcastMessage(data: String, msgType: Int): Either<BleError, Unit> = sendMessage(data, msgType)

}
