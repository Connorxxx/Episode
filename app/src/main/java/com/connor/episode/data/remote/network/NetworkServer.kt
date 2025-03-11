package com.connor.episode.data.remote.network

import arrow.core.Either
import com.connor.episode.domain.model.error.NetworkError
import kotlinx.coroutines.flow.Flow

interface NetworkServer {

    fun startServerAndRead(ip: String, port: Int): Flow<Either<NetworkError, Pair<String, ByteArray>>>

    suspend fun sendBroadcastMessage(byteArray: ByteArray): Either<NetworkError, Unit>

    suspend fun close()

}