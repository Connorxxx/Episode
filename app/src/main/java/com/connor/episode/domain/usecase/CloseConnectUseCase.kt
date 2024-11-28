package com.connor.episode.domain.usecase

import com.connor.episode.domain.repository.SerialPortRepository
import javax.inject.Singleton
import javax.inject.Inject

@Singleton
class CloseConnectUseCase @Inject constructor(
    private val serialPortRepository: SerialPortRepository
) {
    operator fun invoke() {
        serialPortRepository.close()
    }

}