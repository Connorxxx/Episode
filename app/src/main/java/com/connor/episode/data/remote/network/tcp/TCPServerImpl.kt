package com.connor.episode.data.remote.network.tcp

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.connor.episode.core.utils.logCat
import com.connor.episode.data.remote.network.NetworkServer
import com.connor.episode.domain.model.error.NetworkError
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.TcpSocketBuilder
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeByte
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SocketData(
    val socket: Socket,
    val input: ByteReadChannel,
    val output: ByteWriteChannel
)

class TCPServerImpl @Inject constructor(
    private val socketBuilder: TcpSocketBuilder,
) : NetworkServer {

    private val clients = HashMap<String, SocketData>()
    private var serverSocket: ServerSocket? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startServerAndRead(
        ip: String,
        port: Int
    ) = flow {
        either {
            serverSocket = startServer(ip, port).bind()
            acceptClient(serverSocket!!).flatMapMerge { acceptResult ->
                val (address, socket) = acceptResult.bind()
                val input = socket.openReadChannel()
                val output = socket.openWriteChannel(autoFlush = true)
                clients[address] = SocketData(socket, input, output)
                "ip: $address".logCat()
                readClientMessage(address, input).onEach {
                    if (it.isLeft()) clients.remove(address)?.socket?.close()
                }
            }.flowOn(Dispatchers.IO)
        }.fold({ emit(it.left()) }, { emitAll(it) })
    }

    override suspend fun sendBroadcastMessage(byteArray: ByteArray) = either {
        coroutineScope {
            val results = clients.values.map {
                async(Dispatchers.IO) {
                    sendBytesMessage(byteArray, it.output)
                }
            }.awaitAll()
            val err = results.filterIsInstance<Either.Left<NetworkError>>()
            if (err.isNotEmpty()) raise(NetworkError.Write(err.first().value.msg, "null"))
        }
    }

    override suspend fun close() = withContext(NonCancellable + Dispatchers.IO) {
        clients.values.forEach { it.socket.close() }
        clients.clear()
        serverSocket?.close()
        serverSocket = null
    }


    private suspend fun startServer(ip: String, port: Int) = Either.catch {
        socketBuilder.bind(ip, port)
    }.mapLeft { NetworkError.Connect(it.message ?: "Start server error") }

    fun acceptClient(socket: ServerSocket) = flow {
        while (socket.socketContext.isActive) {
            val accept = Either.catch { socket.accept() }
                .mapLeft { NetworkError.Accept(it.message ?: "Accept error") }
                .map {
                    val address = it.remoteAddress.toString()
                    address to it
                }
            emit(accept)
        }
    }

    private fun readClientMessage(ip: String, input: ByteReadChannel) = flow {
        while (true) {
            val bytes = Either.catch { input.readUTF8Line()?.toByteArray() }
                .mapLeft { NetworkError.Read(it.message ?: "Read error", ip) }
                .flatMap { it?.right() ?: NetworkError.Read("connection closed", ip).left() }
                .map { ip to it }
            emit(bytes)
            if (bytes.isLeft()) break
        }
    }

    private suspend fun sendBytesMessage(byteArray: ByteArray, channel: ByteWriteChannel) =
        Either.catch { channel.writeFully(byteArray);channel.writeByte('\n'.code.toByte()) }
            .mapLeft {
                NetworkError.Write(
                    it.message ?: "Write error", "null"
                )
            }

}