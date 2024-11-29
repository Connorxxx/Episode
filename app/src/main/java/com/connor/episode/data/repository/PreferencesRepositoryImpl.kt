package com.connor.episode.data.repository

import com.connor.episode.data.local.datastore.PreferencesModule
import com.connor.episode.domain.model.preference.SerialPortPreferences
import com.connor.episode.domain.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val preferencesModule: PreferencesModule,
    appScope: CoroutineScope
) : PreferencesRepository  {

    override val prefFlow = preferencesModule.serialPref.data
        .shareIn(
            appScope,
            SharingStarted.Lazily,
            replay = 1
        )

    override suspend fun updateSerialPref(pref: (SerialPortPreferences) -> SerialPortPreferences) =
        preferencesModule.serialPref.updateData(pref)

}

