package com.connor.episode.domain.repository

import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.preference.NetPreferences
import com.connor.episode.domain.model.preference.SerialPortPreferences
import kotlinx.coroutines.flow.SharedFlow

interface PreferencesRepository {
    val serialPrefFlow: SharedFlow<SerialPortPreferences>
    val tcpPrefFlow: SharedFlow<NetPreferences>
    val udpPrefFlow: SharedFlow<NetPreferences>
    val webSocketPrefFlow: SharedFlow<NetPreferences>
    suspend fun updateSerialPref(pref: (SerialPortPreferences) -> SerialPortPreferences): SerialPortPreferences
    suspend fun updateTCPPref(pref: (NetPreferences) -> NetPreferences): NetPreferences
    suspend fun updateUDPPref(pref: (NetPreferences) -> NetPreferences): NetPreferences
    suspend fun updateWebSocketPref(pref: (NetPreferences) -> NetPreferences): NetPreferences
    suspend fun getSendFormat(owner: Owner): Int
    suspend fun getReceiveFormat(owner: Owner): Int
}