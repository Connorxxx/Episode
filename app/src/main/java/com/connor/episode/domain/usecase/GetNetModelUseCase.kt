package com.connor.episode.domain.usecase

import com.connor.episode.domain.model.business.ClientModel
import com.connor.episode.domain.model.business.NetModel
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.ServerModel
import com.connor.episode.domain.model.preference.NetPreferences
import com.connor.episode.domain.model.uimodel.NetState
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetNetModelUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val messageRepository: MessageRepository
) {

    suspend operator fun invoke(owner: Owner): NetState {
        val pref: NetPreferences = when (owner) {
            Owner.TCP -> preferencesRepository.tcpPrefFlow.first()
            Owner.UDP -> preferencesRepository.udpPrefFlow.first()
            Owner.WebSocket -> preferencesRepository.webSocketPrefFlow.first()
            else -> throw IllegalArgumentException("Invalid owner type")
        }
        val message = messageRepository.getAllMessages(owner)
        return NetState(
            model = NetModel(
                server = ServerModel(
                    port = pref.serverPort
                ),
                client = ClientModel(
                    ip = pref.clientIP,
                    port = pref.clientPort
                ),
            ),
            messages = message,
            bottomBarSettings = pref.settings,
            currentType = pref.lastSelectType
        )
    }
}