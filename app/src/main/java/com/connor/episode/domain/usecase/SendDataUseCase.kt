package com.connor.episode.domain.usecase

import com.connor.episode.core.di.Client
import com.connor.episode.core.di.NetType.*
import com.connor.episode.core.di.Server
import com.connor.episode.domain.model.business.ModelType
import com.connor.episode.domain.repository.NetClientRepository
import com.connor.episode.domain.repository.NetServerRepository
import com.connor.episode.domain.repository.SerialPortRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendDataUseCase @Inject constructor(
    val serialPortRepository: SerialPortRepository,
    @Server(TCP) val tcpServerRepository: NetServerRepository,
    @Server(UDP) val udpServerRepository: NetServerRepository,
    @Client(TCP) val tcpClientRepository: NetClientRepository,
    @Client(UDP) val udpClientRepository: NetClientRepository,
    @Server(WebSocket) val webSocketServerRepository: NetServerRepository,
    @Client(WebSocket) val webSocketClientRepository: NetClientRepository
) {

    suspend operator fun invoke(bytes: ByteArray, type: ModelType) =
        when (type) {
            ModelType.SerialPort -> serialPortRepository.write(bytes)
            ModelType.TCPServer -> tcpServerRepository.sendBroadcastMessage(bytes)
            ModelType.TCPClient -> tcpClientRepository.sendBytesMessage(bytes)
            ModelType.UDPServer -> udpServerRepository.sendBroadcastMessage(bytes)
            ModelType.UDPClient -> udpClientRepository.sendBytesMessage(bytes)
            ModelType.WebSocketServer -> webSocketServerRepository.sendBroadcastMessage(bytes)
            ModelType.WebSocketClient -> webSocketClientRepository.sendBytesMessage(bytes)
        }.mapLeft { it.msg }

}