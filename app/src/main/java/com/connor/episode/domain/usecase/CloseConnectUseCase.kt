package com.connor.episode.domain.usecase

import com.connor.episode.domain.repository.SerialPortRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class CloseConnectUseCase @Inject constructor(
    private val serialPortRepository: SerialPortRepository
) {
    operator fun invoke() {
        serialPortRepository.close()
    }

}