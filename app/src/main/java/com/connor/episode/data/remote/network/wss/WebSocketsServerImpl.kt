package com.connor.episode.data.remote.network.wss

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.connor.episode.data.remote.network.NetworkServer
import com.connor.episode.domain.model.error.NetworkError
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.origin
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class WebSocketsServerImpl @Inject constructor() : NetworkServer {

    private val clients = ConcurrentHashMap<String, WebSocketServerSession>()

    var embeddedServer: EmbeddedServer<*, *>? = null

    override fun startServerAndRead(
        ip: String,
        port: Int
    ): Flow<Either<NetworkError, Pair<String, ByteArray>>> = flow {
        Either.catch {
            embeddedServer = embeddedServer(CIO, host = ip, port = port) {
                install(WebSockets) {
                    pingPeriod = 15.seconds
                    timeout = 15.seconds
                    maxFrameSize = Long.MAX_VALUE
                    masking = false
                }
                routing {
                    webSocket {
                        clients[call.request.origin.remoteHost] = this
                        emitAll(readClientMessage())
                    }
                }
            }.start(wait = false)
        }.mapLeft {
            NetworkError.Connect(it.message ?: "Start server error")
        }.onLeft { emit(it.left()) }
    }

    fun DefaultWebSocketServerSession.readClientMessage() = incoming.receiveAsFlow().map {
        val ip = call.request.origin.remoteHost
        when (it) {
            is Frame.Text, is Frame.Binary -> (ip to it.readBytes()).right()
            is Frame.Close -> NetworkError.Read("Client send close", ip).left()
            else -> null
        }
    }.filterNotNull().catch {
        val ip = call.request.origin.remoteHost
        emit(NetworkError.Read(it.message ?: "Read error", ip).left())
    }.onEach {
        it.onLeft { clients.remove(it.ip)?.close() }
    }

    override suspend fun sendBroadcastMessage(byteArray: ByteArray) = either {
        if (embeddedServer == null) raise(NetworkError.Write("Server not connected", "null"))
        coroutineScope {
            val results = clients.values.map {
                async(Dispatchers.IO) { sendBytesMessage(byteArray, it) }
            }.awaitAll()
            val err = results.filterIsInstance<Either.Left<NetworkError>>()
            if (err.isNotEmpty()) raise(NetworkError.Write(err.first().value.msg, "null"))
        }
    }

    override suspend fun close() {
        withContext(NonCancellable + Dispatchers.IO) {
            clients.values.forEach { it.close() }
            clients.clear()
            embeddedServer?.stop()
        }
    }

    private suspend fun sendBytesMessage(byteArray: ByteArray, wss: WebSocketServerSession) =
        Either.catch {
            wss.send(Frame.Binary(true, byteArray))
        }.mapLeft {
            NetworkError.Write(
                it.message ?: "Write error, the channel already closed",
                wss.call.request.origin.remoteHost
            )
        }

}