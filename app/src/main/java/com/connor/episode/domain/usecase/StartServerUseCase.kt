package com.connor.episode.domain.usecase

import arrow.core.Either
import com.connor.episode.core.di.NetType.TCP
import com.connor.episode.core.di.NetType.UDP
import com.connor.episode.core.di.NetType.WebSocket
import com.connor.episode.core.di.Server
import com.connor.episode.core.utils.logCat
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.SelectType
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.model.error.NetworkError
import com.connor.episode.domain.model.error.UiError
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.NetServerRepository
import com.connor.episode.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StartServerUseCase @Inject constructor(
    @Server(UDP) val udpServerRepository: NetServerRepository,
    @Server(TCP) val tcpServerRepository: NetServerRepository,
    @Server(WebSocket) val webSocketServerRepository: NetServerRepository,
    val preferencesRepository: PreferencesRepository,
    val messageRepository: MessageRepository
) {

    @OptIn(ExperimentalStdlibApi::class)
    suspend operator fun invoke(port: Int, owner: Owner) = run {
        val currentType: suspend () -> Int = {
        when (owner) {
            Owner.UDP -> preferencesRepository.udpPrefFlow.first().settings.receiveFormat
            Owner.TCP -> preferencesRepository.tcpPrefFlow.first().settings.receiveFormat
            Owner.WebSocket -> preferencesRepository.webSocketPrefFlow.first().settings.receiveFormat
            Owner.SerialPort -> error("SerialPort can't start server")
        }
    }
        when (owner) {
            Owner.UDP -> preferencesRepository.updateUDPPref {
                it.copy(
                    serverPort = port,
                    lastSelectType = SelectType.Server
                )
            }
            Owner.TCP -> preferencesRepository.updateTCPPref {
                it.copy(
                    serverPort = port,
                    lastSelectType = SelectType.Server
                )
            }
            Owner.WebSocket -> preferencesRepository.updateWebSocketPref {
                it.copy(
                    serverPort = port,
                    lastSelectType = SelectType.Server
                )
            }
            Owner.SerialPort -> error("SerialPort can't start server")
        }
        when (owner) {
            Owner.UDP -> udpServerRepository.startServerAndRead("0.0.0.0", port, currentType, owner)
            Owner.TCP -> tcpServerRepository.startServerAndRead("0.0.0.0", port, currentType, owner)
            Owner.WebSocket -> webSocketServerRepository.startServerAndRead("0.0.0.0", port, currentType, owner)
            Owner.SerialPort -> error("SerialPort can't start server")
        }.mapLeftToUiError()
    }

    fun Flow<Either<NetworkError, MessageEntity>>.mapLeftToUiError() = map {
        it.onRight { message ->
            messageRepository.addMessage(message)
        }.mapLeft { err ->
            when (err) {
                is NetworkError.Connect -> UiError(msg = err.msg, isFatal = true)
                else -> UiError(msg = err.msg, isFatal = false)
            }
        }.leftOrNull()
    }.filterNotNull()
}