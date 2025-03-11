package com.connor.episode.data.remote.network

import arrow.core.Either
import com.connor.episode.domain.model.error.NetworkError
import kotlinx.coroutines.flow.Flow

interface NetworkClient {

    fun connectAndRead(ip: String, port: Int): Flow<Either<NetworkError, ByteArray>>

    suspend fun sendBytesMessage(byteArray: ByteArray): Either<NetworkError, Unit>

    suspend fun close()
}