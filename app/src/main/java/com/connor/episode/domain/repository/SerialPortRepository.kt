package com.connor.episode.domain.repository

import arrow.core.Either
import com.connor.episode.domain.error.SerialPortError
import com.connor.episode.domain.model.SerialPortModel
import kotlinx.coroutines.flow.Flow

interface SerialPortRepository {

    suspend fun getSerialPortModel(): SerialPortModel

    suspend fun updatePreferences(model: SerialPortModel): SerialPortModel

    suspend fun addMessage(message: String, isMe: Boolean)

    suspend fun deleteAllMessages()

    fun openAndRead(path: String, baudRate: Int): Flow<Either<SerialPortError, ByteArray>>

    fun write(data: ByteArray): Either<SerialPortError, Unit>

    fun close()
}