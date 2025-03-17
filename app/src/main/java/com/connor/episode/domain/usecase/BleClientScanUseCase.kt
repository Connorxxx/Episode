package com.connor.episode.domain.usecase

import com.connor.episode.domain.repository.BleClientRepository
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class BleClientScanUseCase @Inject constructor(
    private val bleClientRepository: BleClientRepository
) {

    operator fun invoke(timeout: Duration = 60.seconds) = bleClientRepository.scanDevice(timeout)
}