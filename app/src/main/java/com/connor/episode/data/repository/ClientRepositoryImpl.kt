package com.connor.episode.data.repository

import arrow.core.Either
import com.connor.episode.core.utils.getBytesMsg
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
        typeProvider: suspend (Owner) -> Int,
        owner: Owner
    ): Flow<Either<NetworkError, MessageEntity>> = networkClient.connectAndRead(ip, port).map { networkResult ->
        networkResult.map { bytes ->
            val currentType = typeProvider(owner)
            val content =
                if (currentType == 0) bytes.toHexString().uppercase() else bytes.decodeToString()
            MessageEntity(
                name = ip,
                content = content,
                isMe = false,
                type = msgType[currentType],
                owner = owner
            )
        }
    }


    override suspend fun sendMessage(msg: String, msgType: Int) =
        networkClient.sendBytesMessage(getBytesMsg(msgType, msg))

    override suspend fun close() = networkClient.close()
}

