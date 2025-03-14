package com.connor.episode.data.remote.network.wss

import arrow.core.Either
import arrow.core.Eval.Companion.raise
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.connor.episode.core.utils.logCat
import com.connor.episode.data.remote.network.NetworkClient
import com.connor.episode.domain.model.error.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WebSocketsClientImpl @Inject constructor(
    val httpClient: HttpClient
) : NetworkClient {

    private var session: ClientWebSocketSession? = null

    override fun connectAndRead(
        ip: String,
        port: Int
    ): Flow<Either<NetworkError, ByteArray>> = flow {
        httpClient.webSocket(
            method = HttpMethod.Get,
            host = ip,
            port = port,
            path = "/"
        ) {
            session = this
            "ip ${call.request.url.host} connected".logCat()
            incoming.consumeAsFlow().map {
                when (it) {
                    is Frame.Text, is Frame.Binary -> it.readBytes().right()
                    is Frame.Close -> NetworkError.Read("Client send close", call.request.url.host).left()
                    else -> null
                }
            }.filterNotNull().catch {
                val ip = call.request.url.host
                emit(NetworkError.Read(it.message ?: "Read error", ip).left())
            }.onEach {
                it.onLeft { session?.cancel() }
            }.onCompletion {
                session = null
                emit(NetworkError.Connect(it?.message ?: "Server disconnected").left())
            }.collect { either: Either<NetworkError, ByteArray> ->
                emit(either)
            }
        }
    }.catch {
        emit(NetworkError.Connect(it.message ?: "Connect error").left())
    }.flowOn(Dispatchers.IO)

    override suspend fun sendBytesMessage(byteArray: ByteArray) = Either.catch {
        val session = session ?: throw Error("Session not connected")
        session.send(Frame.Binary(true, byteArray))
    }.mapLeft { NetworkError.Write(it.message ?: "Write error", "null") }

    override suspend fun close() = withContext(NonCancellable + Dispatchers.IO){
        session?.cancel()
        session = null
    }

}