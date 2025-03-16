package com.connor.episode.domain.usecase

import com.connor.episode.core.di.Client
import com.connor.episode.core.di.NetType.TCP
import com.connor.episode.core.di.NetType.UDP
import com.connor.episode.core.di.NetType.WebSocket
import com.connor.episode.core.di.Server
import com.connor.episode.domain.model.business.ModelType
import com.connor.episode.domain.repository.NetClientRepository
import com.connor.episode.domain.repository.NetServerRepository
import com.connor.episode.domain.repository.SerialPortRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CloseConnectUseCase @Inject constructor(
    val serialPortRepository: SerialPortRepository,
    @Server(TCP) val tcpServerRepository: NetServerRepository,
    @Server(UDP) val udpServerRepository: NetServerRepository,
    @Client(TCP) val tcpClientRepository: NetClientRepository,
    @Client(UDP) val udpClientRepository: NetClientRepository,
    @Server(WebSocket) val webSocketServerRepository: NetServerRepository,
    @Client(WebSocket) val webSocketClientRepository: NetClientRepository
) {
    suspend operator fun invoke(type: ModelType) = withContext(Dispatchers.IO) {
        when (type) {
            ModelType.SerialPort -> serialPortRepository
            ModelType.TCPServer -> tcpServerRepository
            ModelType.TCPClient -> tcpClientRepository
            ModelType.UDPServer -> udpServerRepository
            ModelType.UDPClient -> udpClientRepository
            ModelType.WebSocketServer -> webSocketServerRepository
            ModelType.WebSocketClient -> webSocketClientRepository
            else -> error("Invalid model type")
        }.close()
    }

}