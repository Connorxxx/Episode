package com.connor.episode.data.repository

import com.connor.episode.data.local.datastore.PreferencesModule
import javax.inject.Inject

class PreferencesRepository @Inject constructor(
    private val preferencesModule: PreferencesModule
) {

    val serialPref = preferencesModule.serialPref
}