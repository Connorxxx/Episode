package com.connor.episode.data.repository

import arrow.core.Either
import com.connor.episode.core.utils.logCat
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
    val networkServer: NetworkServer,
) : NetServerRepository {

    @OptIn(ExperimentalStdlibApi::class)
    override fun startServerAndRead(
        ip: String,
        port: Int,
        type: Int,
        owner: Owner
    ): Flow<Either<NetworkError, MessageEntity>> {
        "start server by $owner".logCat()
        return networkServer.startServerAndRead(ip, port).map {
            it.map { (ip, bytes) ->
                val content = if (type == 0) bytes.toHexString().uppercase() else bytes.decodeToString()
                MessageEntity(
                    name = ip,
                    content = content,
                    bytes = bytes,
                    isMe = false,
                    type = msgType[type],
                    owner = owner
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun sendBroadcastMessage(byteArray: ByteArray) =
        networkServer.sendBroadcastMessage(byteArray)

    override suspend fun close() = networkServer.close()

}