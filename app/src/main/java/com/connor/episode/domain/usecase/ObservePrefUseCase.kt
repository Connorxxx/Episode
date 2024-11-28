package com.connor.episode.domain.usecase

import com.connor.episode.domain.repository.PreferencesRepository
import javax.inject.Singleton
import javax.inject.Inject

@Singleton
class ObservePrefUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    operator fun invoke() = preferencesRepository.observeSerialPref()
}