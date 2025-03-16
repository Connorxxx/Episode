package com.connor.episode.domain.repository

import arrow.core.Either
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.model.error.NetworkError
import com.connor.episode.domain.repository.common.Close
import com.connor.episode.domain.repository.common.SendMessage
import kotlinx.coroutines.flow.Flow

interface NetClientRepository : SendMessage<NetworkError>, Close {

    fun connectAndRead(ip: String, port: Int, typeProvider: suspend (Owner) -> Int, owner: Owner): Flow<Either<NetworkError, MessageEntity>>

    suspend fun sendMessageToServer(msg: String, msgType: Int): Either<NetworkError, Unit> = sendMessage(msg, msgType)
}