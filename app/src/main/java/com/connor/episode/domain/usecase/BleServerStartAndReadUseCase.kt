package com.connor.episode.domain.usecase

import com.connor.episode.domain.repository.BleServerRepository
import javax.inject.Inject

class BleServerStartAndReadUseCase @Inject constructor(
    private val bleServerRepository: BleServerRepository
) {

    operator fun invoke() = bleServerRepository.startServerAndRead
}