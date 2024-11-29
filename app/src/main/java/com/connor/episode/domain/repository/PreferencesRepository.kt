package com.connor.episode.domain.repository

import com.connor.episode.domain.model.preference.SerialPortPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface PreferencesRepository {
    val prefFlow: SharedFlow<SerialPortPreferences>
    suspend fun updateSerialPref(pref: (SerialPortPreferences) -> SerialPortPreferences): SerialPortPreferences
}