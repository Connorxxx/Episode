package com.connor.episode.data.repository

import arrow.core.Either
import com.connor.episode.core.utils.getBytesMsg
import com.connor.episode.data.remote.network.NetworkServer
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.msgType
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.model.error.NetworkError
import com.connor.episode.domain.repository.NetServerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class ServerRepositoryImpl @Inject constructor(
    private val networkServer: NetworkServer,
) : NetServerRepository {

    @OptIn(ExperimentalStdlibApi::class)
    override fun startServerAndRead(
        ip: String,
        port: Int,
        typeProvider: suspend (Owner) -> Int,
        owner: Owner
    ): Flow<Either<NetworkError, MessageEntity>> {
        return networkServer.startServerAndRead(ip, port).map {
            it.map { (ip, bytes) ->
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
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun sendMessage(msg: String, msgType: Int) =
        networkServer.sendBroadcastMessage(getBytesMsg(msgType, msg))

    override suspend fun close() = networkServer.close()

}