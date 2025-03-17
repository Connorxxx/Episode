package com.connor.episode.domain.usecase

import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.preference.BlePreferences
import com.connor.episode.domain.model.preference.NetPreferences
import com.connor.episode.domain.model.preference.SerialPortPreferences
import com.connor.episode.domain.repository.PreferencesRepository
import javax.inject.Inject

class UpdatePreferencesUseCase @Inject constructor(
    val preferencesRepository: PreferencesRepository
) {

    suspend fun serial(config: (SerialPortPreferences) -> SerialPortPreferences) =
        preferencesRepository.updateSerialPref(config)

    suspend fun tcp(config: (NetPreferences) -> NetPreferences) =
        preferencesRepository.updateTCPPref(config)

    suspend fun udp(config: (NetPreferences) -> NetPreferences) =
        preferencesRepository.updateUDPPref(config)

    suspend fun webSocket(config: (NetPreferences) -> NetPreferences) =
        preferencesRepository.updateWebSocketPref(config)

    suspend fun ble(config: (BlePreferences) -> BlePreferences) =
        preferencesRepository.updateBlePref(config)

}