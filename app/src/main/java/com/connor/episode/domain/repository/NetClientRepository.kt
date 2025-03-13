package com.connor.episode.domain.repository

import arrow.core.Either
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.model.error.NetworkError
import kotlinx.coroutines.flow.Flow

interface NetClientRepository {

    fun connectAndRead(ip: String, port: Int, typeProvider: suspend () -> Int, owner: Owner): Flow<Either<NetworkError, MessageEntity>>

    suspend fun sendBytesMessage(byteArray: ByteArray): Either<NetworkError, Unit>

    suspend fun close(): Unit?
}