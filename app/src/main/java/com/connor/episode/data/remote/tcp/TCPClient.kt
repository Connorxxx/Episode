package com.connor.episode.data.remote.tcp

import arrow.core.Either
import com.connor.episode.domain.model.error.NetworkError
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.TcpSocketBuilder
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readBuffer
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.flow.flow
import kotlinx.io.readByteArray
import javax.inject.Inject

class TCPClient @Inject constructor(private val socketBuilder: TcpSocketBuilder) {

    suspend fun connect(ip: String, port: Int) = Either.catch {
        socketBuilder.connect(ip, port) {
            keepAlive = true
        }
    }.mapLeft { NetworkError.Connect(it.message ?: "Connect error") }

    fun readServerMessage(socket: Socket) = flow {
        while (true) {
            val bytes = Either.catch { socket.openReadChannel().readBuffer().readByteArray() }
                .mapLeft { NetworkError.Read(it.message ?: "Read error", socket.remoteAddress.toString()) }
            emit(bytes)
        }
    }

    suspend fun sendBytesMessage(byteArray: ByteArray, socket: Socket) =
        Either.catch { socket.openWriteChannel(autoFlush = true).writeFully(byteArray) }.mapLeft {
            NetworkError.Write(it.message ?: "Write error", socket.remoteAddress.toString())
        }
}