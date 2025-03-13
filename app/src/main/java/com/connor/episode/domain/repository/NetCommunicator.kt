package com.connor.episode.domain.repository

import arrow.core.Either

interface NetCommunicator {

    suspend fun sendMessage(bytes: ByteArray): Either<Error, Unit>
    suspend fun close()

}