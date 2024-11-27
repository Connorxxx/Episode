package com.connor.episode.domain.usecase

import com.connor.episode.domain.repository.PreferencesRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ObservePrefUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    operator fun invoke() = preferencesRepository.observeSerialPref()
}