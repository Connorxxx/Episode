package com.connor.episode.domain.usecase

import com.connor.episode.domain.repository.BleClientRepository
import javax.inject.Inject
import kotlin.time.Duration

class BleClientScanUseCase @Inject constructor(
    private val bleClientRepository: BleClientRepository
) {

    operator fun invoke(timeout: Duration) = bleClientRepository.scanDevice(timeout)
}