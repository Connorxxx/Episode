package com.connor.episode.domain.usecase

import com.connor.episode.core.di.Client
import com.connor.episode.core.di.NetType.TCP
import com.connor.episode.core.di.NetType.UDP
import com.connor.episode.core.di.NetType.WebSocket
import com.connor.episode.core.di.Server
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.SelectType
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.repository.BleClientRepository
import com.connor.episode.domain.repository.BleServerRepository
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.NetClientRepository
import com.connor.episode.domain.repository.NetServerRepository
import com.connor.episode.domain.repository.PreferencesRepository
import com.connor.episode.domain.repository.SerialPortRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class ResendUseCase @Inject constructor(
    val messageRepository: MessageRepository,
    val preferencesRepository: PreferencesRepository,
    val serialPortRepository: SerialPortRepository,
    @Server(TCP) val tcpServerRepository: NetServerRepository,
    @Server(UDP) val udpServerRepository: NetServerRepository,
    @Client(TCP) val tcpClientRepository: NetClientRepository,
    @Client(UDP) val udpClientRepository: NetClientRepository,
    @Server(WebSocket) val webSocketServerRepository: NetServerRepository,
    @Client(WebSocket) val webSocketClientRepository: NetClientRepository,
    private val bleServerRepository: BleServerRepository,
    private val bleClientRepository: BleClientRepository
) {

    operator fun invoke(resend: Boolean, owner: Owner) = flow {
        when (owner) {
            Owner.SerialPort -> preferencesRepository.updateSerialPref {
                it.copy(settings = it.settings.copy(resend = resend))
            }

            Owner.TCP -> preferencesRepository.updateTCPPref {
                it.copy(settings = it.settings.copy(resend = resend))
            }

            Owner.UDP -> preferencesRepository.updateUDPPref {
                it.copy(settings = it.settings.copy(resend = resend))
            }

            Owner.WebSocket -> preferencesRepository.updateWebSocketPref {
                it.copy(settings = it.settings.copy(resend = resend))
            }
            Owner.BLE -> preferencesRepository.updateBlePref {
                it.copy(settings = it.settings.copy(resend = resend))
            }
        }
        if (!resend) return@flow
        val lastMsg = messageRepository.getLastSendMessage(owner) ?: run {
            emit("No message to resend")
            return@flow
        }
        getResendSeconds(owner).onStart { emit(Unit) }.mapByOwner(owner, lastMsg)
            .also { emitAll(it) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getResendSeconds(owner: Owner): Flow<Unit> = when (owner) {
        Owner.SerialPort -> preferencesRepository.serialPrefFlow.transformLatest { pref ->
            resendLoop(pref.settings.resendSeconds)
        }

        Owner.TCP -> preferencesRepository.tcpPrefFlow.transformLatest { pref ->
            resendLoop(pref.settings.resendSeconds)
        }

        Owner.UDP -> preferencesRepository.udpPrefFlow.transformLatest { pref ->
            resendLoop(pref.settings.resendSeconds)
        }

        Owner.WebSocket -> preferencesRepository.webSocketPrefFlow.transformLatest { pref ->
            resendLoop(pref.settings.resendSeconds)
        }
        Owner.BLE -> preferencesRepository.blePrefFlow.transformLatest { pref ->
            resendLoop(pref.settings.resendSeconds)
        }
    }

    private suspend fun FlowCollector<Unit>.resendLoop(seconds: Int) {
        while (true) {
            delay(seconds.seconds)
            emit(Unit)
        }
    }

    private fun <T> Flow<T>.mapByOwner(owner: Owner, lastMsg: MessageEntity) = map {
        when (owner) {
            Owner.SerialPort -> serialPortRepository
            Owner.TCP -> when (preferencesRepository.tcpPrefFlow.first().lastSelectType) {
                SelectType.Server -> tcpServerRepository
                SelectType.Client -> tcpClientRepository
            }

            Owner.UDP -> when (preferencesRepository.udpPrefFlow.first().lastSelectType) {
                SelectType.Server -> udpServerRepository
                SelectType.Client -> udpClientRepository
            }

            Owner.WebSocket -> when (preferencesRepository.webSocketPrefFlow.first().lastSelectType) {
                SelectType.Server -> webSocketServerRepository
                SelectType.Client -> webSocketClientRepository
            }
            Owner.BLE -> when (preferencesRepository.blePrefFlow.first().lastSelectType) {
                SelectType.Server -> bleServerRepository
                SelectType.Client -> bleClientRepository
            }
        }.sendMessage(lastMsg.content, preferencesRepository.getSendFormat(owner))
            .onRight {
                messageRepository.addMessage(lastMsg.copy(id = 0))
            }.leftOrNull()?.msg
    }.filterNotNull()
}