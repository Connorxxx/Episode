package com.connor.episode.domain.repository

import arrow.core.Either
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.model.error.NetworkError
import kotlinx.coroutines.flow.Flow

interface NetServerRepository {

    fun startServerAndRead(ip: String, port: Int, typeProvider: suspend () -> Int, owner: Owner): Flow<Either<NetworkError, MessageEntity>>

    suspend fun sendBroadcastMessage(bytes: ByteArray): Either<NetworkError, Unit>

    suspend fun close(): Unit?

}