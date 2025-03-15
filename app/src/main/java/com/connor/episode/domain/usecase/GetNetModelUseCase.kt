package com.connor.episode.domain.usecase

import com.connor.episode.domain.model.business.ClientModel
import com.connor.episode.domain.model.business.NetModel
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.ServerModel
import com.connor.episode.domain.model.preference.NetPreferences
import com.connor.episode.domain.model.uimodel.NetState
import com.connor.episode.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetNetModelUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) {

    suspend operator fun invoke(owner: Owner): NetState {
        val pref: NetPreferences = when (owner) {
            Owner.TCP -> preferencesRepository.tcpPrefFlow
            Owner.UDP -> preferencesRepository.udpPrefFlow
            Owner.WebSocket -> preferencesRepository.webSocketPrefFlow
            else -> throw IllegalArgumentException("Invalid owner type")
        }.first()
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
            bottomBarSettings = pref.settings,
            currentType = pref.lastSelectType
        )
    }
}