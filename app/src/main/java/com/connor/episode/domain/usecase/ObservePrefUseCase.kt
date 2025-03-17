package com.connor.episode.domain.usecase

import com.connor.episode.domain.repository.PreferencesRepository
import javax.inject.Inject

class ObservePrefUseCase @Inject constructor(
    val preferencesRepository: PreferencesRepository
) {

    val serial get() = preferencesRepository.serialPrefFlow
    val tcp get() = preferencesRepository.tcpPrefFlow
    val udp get() = preferencesRepository.udpPrefFlow
    val webSocket get() = preferencesRepository.webSocketPrefFlow
    val ble get() = preferencesRepository.blePrefFlow
}