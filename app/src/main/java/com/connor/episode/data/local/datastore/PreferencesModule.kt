package com.connor.episode.data.local.datastore

import android.content.Context
import com.connor.episode.data.local.datastore.preference.SerialPortPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferencesModule @Inject constructor(@ApplicationContext private val context: Context) {

    private val Context.serialPreferences by protobufDataStore<SerialPortPreferences>("serialPortPreferences.pb")
//    private val Context.tcpPreferences by protobufDataStore<TCPreferences>("tcpPreferences.pb")
//    private val Context.udpPreferences by protobufDataStore<UDPPreferences>("udpPreferences.pb")

    val serialPref = context.serialPreferences
//    val tcpPref = context.tcpPreferences
//    val udpPref = context.udpPreferences

}