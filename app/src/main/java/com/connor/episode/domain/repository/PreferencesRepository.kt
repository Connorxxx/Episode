package com.connor.episode.domain.repository

import com.connor.episode.domain.model.preference.SerialPortPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    suspend fun getSerialPref(): SerialPortPreferences
    fun observeSerialPref(): Flow<SerialPortPreferences>
    suspend fun updateSerialPref(pref: (SerialPortPreferences) -> SerialPortPreferences): SerialPortPreferences
}