package com.connor.episode.domain.usecase

import com.connor.episode.domain.repository.BleServerRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class BleServerStartAdvertisingUseCase @Inject constructor(
    private val bleServerRepository: BleServerRepository
) {

    operator fun invoke(timeout: Duration = 60.seconds) = bleServerRepository.startAdvertising(timeout).map {
        it.leftOrNull()
    }.filterNotNull()
}