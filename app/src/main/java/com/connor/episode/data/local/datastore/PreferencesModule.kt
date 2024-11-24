package com.connor.episode.data.local.datastore

import android.content.Context
import com.connor.episode.core.datastore.protobufDataStore
import com.connor.episode.data.local.datastore.preference.SerialPortPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferencesModule @Inject constructor(@ApplicationContext private val context: Context) {

    private val Context.serialPreferences by protobufDataStore<SerialPortPreferences>("serialPortPreferences.pb")

    val serialPref = context.serialPreferences
}