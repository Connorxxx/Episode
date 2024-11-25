package com.connor.episode.domain.repository

import com.connor.episode.domain.model.SerialPortModel

interface PreferencesRepository {
    suspend fun getSerialPref(): SerialPortModel
    suspend fun updateSerialPref(pref: SerialPortModel): SerialPortModel
}