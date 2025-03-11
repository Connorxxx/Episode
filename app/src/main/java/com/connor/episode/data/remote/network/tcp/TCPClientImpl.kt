package com.connor.episode.data.remote.network.tcp

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.connor.episode.data.remote.network.NetworkClient
import com.connor.episode.domain.model.error.NetworkError
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.TcpSocketBuilder
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TCPClientImpl @Inject constructor(private val socketBuilder: TcpSocketBuilder) : NetworkClient {

    private var socket: Socket? = null
    private var input: ByteReadChannel? = null
    private var output: ByteWriteChannel? = null

    override fun connectAndRead(
        ip: String,
        port: Int
    ) = flow {
        connect(ip, port).map {
            socket = it
            input = it.openReadChannel()
            output = it.openWriteChannel(autoFlush = true)
            readServerMessage(it, input!!)
        }.fold(
            ifLeft = { emit(it.left()) },
            ifRight = { emitAll(it) }
        )
    }

    override suspend fun sendBytesMessage(byteArray: ByteArray) = either {
        val output = output ?: raise(NetworkError.Write("Socket not connected", "null"))
        sendBytesMessage(byteArray, output).bind()
    }

    override suspend fun close() = withContext(NonCancellable + Dispatchers.IO) {
        socket?.close()
        socket = null
        input = null
        output = null
    }

    private suspend fun connect(ip: String, port: Int) = Either.catch {
        socketBuilder.connect(ip, port) {
            keepAlive = true
        }
    }.mapLeft { NetworkError.Connect(it.message ?: "Connect error $ip:$port") }

    private fun readServerMessage(socket: Socket, input: ByteReadChannel) = flow {
        while (socket.isActive) {
            val bytes = Either.catch { input.readUTF8Line()?.toByteArray() }
                .mapLeft {
                    NetworkError.Read(
                        it.message ?: "Read error",
                        socket.remoteAddress.toString()
                    )
                }
                .flatMap {
                    it?.right() ?: NetworkError.Read(
                        "connection closed",
                        socket.remoteAddress.toString()
                    ).left()
                }
            emit(bytes)
            if (bytes.isLeft()) break
        }
    }

    private suspend fun sendBytesMessage(byteArray: ByteArray, output: ByteWriteChannel) =
        Either.catch { output.writeFully(byteArray) }.mapLeft {
            NetworkError.Write(it.message ?: "Write error", "null")
        }


}