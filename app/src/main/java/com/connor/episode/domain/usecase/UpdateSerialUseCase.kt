package com.connor.episode.domain.usecase

import com.connor.episode.domain.model.preference.SerialPortPreferences
import com.connor.episode.domain.repository.PreferencesRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class UpdateSerialUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {

    suspend operator fun invoke(config: (SerialPortPreferences) -> SerialPortPreferences) {
        preferencesRepository.updateSerialPref(config)
    }
}