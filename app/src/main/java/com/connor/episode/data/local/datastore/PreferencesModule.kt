package com.connor.episode.data.local.datastore

import android.content.Context
import com.connor.episode.core.delegate.protobufDataStore
import com.connor.episode.domain.model.preference.NetPreferences
import com.connor.episode.domain.model.preference.SerialPortPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferencesModule @Inject constructor(@ApplicationContext private val context: Context) {

    private val Context.serialPreferences by protobufDataStore<SerialPortPreferences>("serialPortPreferences.pb")
    private val Context.tcpPreferences by protobufDataStore<NetPreferences>("tcpPreferences.pb")
    private val Context.udpPreferences by protobufDataStore<NetPreferences>("udpPreferences.pb")
    private val Context.wssPreferences by protobufDataStore<NetPreferences>("wssPreferences.pb")

    val serialPref = context.serialPreferences
    val tcpPref = context.tcpPreferences
    val udpPref = context.udpPreferences
    val wssPref = context.wssPreferences

}