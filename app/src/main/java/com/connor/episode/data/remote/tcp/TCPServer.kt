package com.connor.episode.data.remote.tcp

import arrow.core.Either
import com.connor.episode.domain.model.error.NetworkError
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.TcpSocketBuilder
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readBuffer
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.io.readByteArray
import javax.inject.Inject

class TCPServer @Inject constructor(
    private val socketBuilder: TcpSocketBuilder,
) {

    //private val clients = ConcurrentHashMap<String, Socket>()  //TODO 在 Repository 中实现并管理

    suspend fun startServer(ip: String, port: Int) = Either.catch {
        socketBuilder.bind(ip, port)
    }.mapLeft { NetworkError.Connect(it.message ?: "Start server error") }

    fun acceptClient(socket: ServerSocket) = flow {
        while (true) {
            val accept = Either.catch { socket.accept() }
                .mapLeft { NetworkError.Accept(it.message ?: "Accept error") }
                .map {
                    val ip = it.remoteAddress.toString()
                    ip to it
                }
            emit(accept)
        }
    }.flowOn(Dispatchers.IO)

    fun readClientMessage(ip: String, socket: Socket) = flow {
        while (true) {
            val readByteArray =
                Either.catch { socket.openReadChannel().readBuffer().readByteArray() }
                    .mapLeft { NetworkError.Read(it.message ?: "Read error", ip) }
                    .map { ip to it }
            emit(readByteArray)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun sendBytesMessage(byteArray: ByteArray, socket: Socket) =
        Either.catch {
            val output = socket.openWriteChannel(autoFlush = true)
            output.writeFully(byteArray)
        }.mapLeft {
            NetworkError.Write(it.message ?: "Write error", socket.remoteAddress.toString())
        }

}