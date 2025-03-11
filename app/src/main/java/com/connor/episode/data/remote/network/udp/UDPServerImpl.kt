package com.connor.episode.data.remote.network.udp

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import com.connor.episode.core.utils.asSource
import com.connor.episode.data.remote.network.NetworkServer
import com.connor.episode.domain.model.error.NetworkError
import io.ktor.network.sockets.BoundDatagramSocket
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.SocketAddress
import io.ktor.network.sockets.UDPSocketBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import kotlinx.io.readByteArray
import javax.inject.Inject
import kotlin.collections.map

class UDPServerImpl @Inject constructor(
    private val socketBuilder: UDPSocketBuilder
) : NetworkServer {

    private var socket: BoundDatagramSocket? = null
    private val clients = HashSet<SocketAddress>()

    override fun startServerAndRead(
        ip: String,
        port: Int
    ): Flow<Either<NetworkError, Pair<String, ByteArray>>> = flow {
        either {
            socket = startServer(ip, port).bind()
            acceptClient(socket!!).map { acceptResult ->
                acceptResult.map { (address, bytes) ->
                    clients.add(address)
                    address.toString() to bytes
                }
            }.flowOn(Dispatchers.IO)
        }.fold({ emit(it.left()) }, { emitAll(it) })
    }

    override suspend fun sendBroadcastMessage(byteArray: ByteArray) = either {
        if (socket == null) raise(NetworkError.Write("Socket not connected", "null"))
        coroutineScope {
            val results = clients.map {
                async(Dispatchers.IO) {
                    sendBytesMessage(byteArray, socket!!, it)
                }
            }.awaitAll()
            val err = results.filterIsInstance<Either.Left<NetworkError>>()
            if (err.isNotEmpty()) raise(NetworkError.Write(err.first().value.msg, "null"))
        }
    }

    override suspend fun close() = withContext(NonCancellable + Dispatchers.IO) {
        socket?.close()
        clients.clear()
        socket = null
    }

    private suspend fun startServer(ip: String, port: Int) = Either.catch {
        socketBuilder.bind(InetSocketAddress(ip, port)) {
            broadcast = true
        }
    }.mapLeft { NetworkError.Connect(it.message ?: "Start server error") }

    private fun acceptClient(socket: BoundDatagramSocket) =
        socket.incoming.receiveAsFlow().map { datagram ->
            Either.catch {
                val address = datagram.address
                val bytes = datagram.packet.readByteArray()
                address to bytes
            }.mapLeft { NetworkError.Read(it.message ?: "Read error", datagram.address.toString()) }
        }

    private suspend fun sendBytesMessage(
        byteArray: ByteArray,
        socket: BoundDatagramSocket,
        address: SocketAddress
    ) = Either.catch {
        socket.outgoing.send(Datagram(byteArray.asSource(), address))
    }.mapLeft { NetworkError.Write(it.message ?: "Write error", address.toString()) }
}