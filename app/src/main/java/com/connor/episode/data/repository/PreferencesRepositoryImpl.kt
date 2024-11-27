package com.connor.episode.data.repository

import com.connor.episode.data.local.datastore.PreferencesModule
import com.connor.episode.domain.model.preference.SerialPortPreferences
import com.connor.episode.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val preferencesModule: PreferencesModule
) : PreferencesRepository  {

    override suspend fun getSerialPref() = preferencesModule.serialPref.data.first()

    override fun observeSerialPref() = preferencesModule.serialPref.data

    override suspend fun updateSerialPref(pref: (SerialPortPreferences) -> SerialPortPreferences) =
        preferencesModule.serialPref.updateData(pref)
}