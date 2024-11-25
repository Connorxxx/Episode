package com.connor.episode.data.repository

import com.connor.episode.data.local.datastore.PreferencesModule
import com.connor.episode.data.mapper.toModel
import com.connor.episode.data.mapper.toPreferences
import com.connor.episode.domain.model.SerialPortModel
import com.connor.episode.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val preferencesModule: PreferencesModule
) : PreferencesRepository  {

    override suspend fun getSerialPref() = preferencesModule.serialPref.data.first().toModel()

    override suspend fun updateSerialPref(pref: SerialPortModel) =
        preferencesModule.serialPref.updateData { pref.toPreferences() }.toModel()
}