package com.connor.episode.domain.usecase

import arrow.core.Either
import com.connor.episode.core.di.NetType.TCP
import com.connor.episode.core.di.NetType.UDP
import com.connor.episode.core.di.NetType.WebSocket
import com.connor.episode.core.di.Server
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
            else -> error("SerialPort can't start server")
        }
        val receiveFormat = preferencesRepository::getReceiveFormat
        when (owner) {
            Owner.UDP -> udpServerRepository
            Owner.TCP -> tcpServerRepository
            Owner.WebSocket -> webSocketServerRepository
            else -> error("SerialPort can't start server")
        }.startServerAndRead("0.0.0.0", port, receiveFormat, owner).mapLeftToUiError()
    }

    private fun Flow<Either<NetworkError, MessageEntity>>.mapLeftToUiError() = map {
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