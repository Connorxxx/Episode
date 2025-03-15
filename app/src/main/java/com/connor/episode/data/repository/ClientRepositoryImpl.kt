package com.connor.episode.data.repository

import arrow.core.Either
import com.connor.episode.data.remote.network.NetworkClient
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.msgType
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.model.error.NetworkError
import com.connor.episode.domain.repository.NetClientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClientRepositoryImpl @Inject constructor(
    private val networkClient: NetworkClient
) : NetClientRepository {

    @OptIn(ExperimentalStdlibApi::class)
    override fun connectAndRead(
        ip: String,
        port: Int,
        typeProvider: suspend () -> Int,
        owner: Owner
    ): Flow<Either<NetworkError, MessageEntity>> = networkClient.connectAndRead(ip, port).map { networkResult ->
        networkResult.map { bytes ->
            val currentType = typeProvider()
            val content =
                if (currentType == 0) bytes.toHexString().uppercase() else bytes.decodeToString()
            MessageEntity(
                name = ip,
                content = content,
                bytes = bytes,
                isMe = false,
                type = msgType[currentType],
                owner = owner
            )
        }
    }


    override suspend fun sendBytesMessage(byteArray: ByteArray) =
        networkClient.sendBytesMessage(byteArray)

    override suspend fun close() = networkClient.close()
}

