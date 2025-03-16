package com.connor.episode.domain.usecase

import arrow.core.Either
import com.connor.episode.core.di.Client
import com.connor.episode.core.di.NetType.TCP
import com.connor.episode.core.di.NetType.UDP
import com.connor.episode.core.di.NetType.WebSocket
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.SelectType
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.model.error.NetworkError
import com.connor.episode.domain.model.error.UiError
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.NetClientRepository
import com.connor.episode.domain.repository.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConnectServerUseCase @Inject constructor(
    @Client(TCP) val tcpClientRepository: NetClientRepository,
    @Client(UDP) val udpClientRepository: NetClientRepository,
    @Client(WebSocket) val webSocketClientRepository: NetClientRepository,
    val preferencesRepository: PreferencesRepository,
    val messageRepository: MessageRepository
) {
    @OptIn(ExperimentalStdlibApi::class)
    suspend operator fun invoke(ip: String, port: Int, owner: Owner) = run {
        when (owner) {
            Owner.UDP -> preferencesRepository.updateUDPPref {
                it.copy(
                    clientIP = ip,
                    clientPort = port,
                    lastSelectType = SelectType.Client
                )
            }

            Owner.TCP -> preferencesRepository.updateTCPPref {
                it.copy(
                    clientIP = ip,
                    clientPort = port,
                    lastSelectType = SelectType.Client
                )
            }

            Owner.WebSocket -> preferencesRepository.updateWebSocketPref {
                it.copy(
                    clientIP = ip,
                    clientPort = port,
                    lastSelectType = SelectType.Client
                )
            }

            Owner.SerialPort -> error("SerialPort can't connect")
        }

        val receiveFormat = preferencesRepository::getReceiveFormat
        when (owner) {
            Owner.UDP -> udpClientRepository
            Owner.TCP -> tcpClientRepository
            Owner.WebSocket -> webSocketClientRepository
            Owner.SerialPort -> error("SerialPort can't connect")
        }.connectAndRead(ip, port, receiveFormat, owner).mapLeftToUiError()
    }

    fun Flow<Either<NetworkError, MessageEntity>>.mapLeftToUiError() = map {
        it.onRight { message ->
            messageRepository.addMessage(message)
        }.mapLeft { err ->
            UiError(msg = err.msg, isFatal = true)
        }.leftOrNull()
    }.filterNotNull().flowOn(Dispatchers.IO)
}
