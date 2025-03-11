package com.connor.episode.data.remote.network.udp

import arrow.core.Either
import arrow.core.left
import com.connor.episode.core.utils.asSource
import com.connor.episode.core.utils.logCat
import com.connor.episode.data.remote.network.NetworkClient
import com.connor.episode.domain.model.error.NetworkError
import io.ktor.network.sockets.ConnectedDatagramSocket
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.UDPSocketBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.io.readByteArray
import javax.inject.Inject

class UDPClientImpl @Inject constructor(
    private val socketBuilder: UDPSocketBuilder
) : NetworkClient {

    private var socket: ConnectedDatagramSocket? = null

    override fun connectAndRead(
        ip: String,
        port: Int
    ): Flow<Either<NetworkError, ByteArray>> = flow {
        connect(ip, port).map {
            socket = it
            readServerMessage(it)
        }.fold(
            ifLeft = { emit(it.left()) },
            ifRight = {
                val flow = it.map { it.map { it.second } }
                emitAll(flow)
            }
        )
    }

    override suspend fun sendBytesMessage(byteArray: ByteArray): Either<NetworkError, Unit> {
        if (socket == null) return NetworkError.Write("Socket not connected", "null").left()
        "send upd msg".logCat()
        return sendBytesMessage(byteArray, socket!!)
    }

    override suspend fun close() {
        socket?.close()
        socket = null
    }

    suspend fun connect(ip: String, port: Int) = Either.catch {
        socketBuilder.connect(InetSocketAddress(ip, port)) {
            broadcast = true
        }
    }.mapLeft { NetworkError.Connect(it.message ?: "Connect error $ip:$port") }

    fun readServerMessage(socket: ConnectedDatagramSocket) = socket.incoming.receiveAsFlow().map {
        Either.catch {
            val address = it.address
            val bytes = it.packet.readByteArray()
            address to bytes
        }.mapLeft { NetworkError.Read(it.message ?: "Read error", socket.remoteAddress.toString()) }
    }

    suspend fun sendBytesMessage(byteArray: ByteArray, socket: ConnectedDatagramSocket) = Either.catch {
        socket.outgoing.send(Datagram(byteArray.asSource(), socket.remoteAddress))
    }.mapLeft { NetworkError.Write(it.message ?: "Write error", socket.remoteAddress.toString()) }
}