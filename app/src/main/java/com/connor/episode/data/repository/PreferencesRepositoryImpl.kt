package com.connor.episode.data.repository

import com.connor.episode.data.local.datastore.PreferencesModule
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.preference.NetPreferences
import com.connor.episode.domain.model.preference.SerialPortPreferences
import com.connor.episode.domain.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val preferencesModule: PreferencesModule,
    appScope: CoroutineScope
) : PreferencesRepository {

    override val serialPrefFlow = preferencesModule.serialPref.data
        .shareIn(
            appScope,
            SharingStarted.Lazily,
            replay = 1
        )

    override val tcpPrefFlow = preferencesModule.tcpPref.data
        .shareIn(
            appScope,
            SharingStarted.Lazily,
            replay = 1
        )

    override val udpPrefFlow = preferencesModule.udpPref.data
        .shareIn(
            appScope,
            SharingStarted.Lazily,
            replay = 1
        )

    override val webSocketPrefFlow = preferencesModule.wssPref.data
        .shareIn(
            appScope,
            SharingStarted.Lazily,
            replay = 1
        )

    override suspend fun updateSerialPref(pref: (SerialPortPreferences) -> SerialPortPreferences) =
        preferencesModule.serialPref.updateData(pref)

    override suspend fun updateTCPPref(pref: (NetPreferences) -> NetPreferences) =
        preferencesModule.tcpPref.updateData(pref)

    override suspend fun updateUDPPref(pref: (NetPreferences) -> NetPreferences) =
        preferencesModule.udpPref.updateData(pref)

    override suspend fun updateWebSocketPref(pref: (NetPreferences) -> NetPreferences) =
        preferencesModule.wssPref.updateData(pref)


    override suspend fun getSendFormat(owner: Owner) = when (owner) {
        Owner.SerialPort -> serialPrefFlow.map { it.settings }
        Owner.TCP -> tcpPrefFlow.map { it.settings }
        Owner.UDP -> udpPrefFlow.map { it.settings }
        Owner.WebSocket -> webSocketPrefFlow.map { it.settings }
    }.first().sendFormat

    override suspend fun getReceiveFormat(owner: Owner) = when (owner) {
        Owner.SerialPort -> serialPrefFlow.map { it.settings }
        Owner.TCP -> tcpPrefFlow.map { it.settings }
        Owner.UDP -> udpPrefFlow.map { it.settings }
        Owner.WebSocket -> webSocketPrefFlow.map { it.settings }
    }.first().receiveFormat

}

