package com.connor.episode.domain.repository

import arrow.core.Either
import com.connor.episode.domain.model.business.SerialPortDevice
import com.connor.episode.domain.model.config.SerialConfig
import com.connor.episode.domain.model.error.SerialPortError
import kotlinx.coroutines.flow.Flow

interface SerialPortRepository {
    val getAllDevices: List<SerialPortDevice>
    fun openAndRead(config: SerialConfig): Flow<Either<SerialPortError, ByteArray>>
    fun write(data: ByteArray): Either<SerialPortError, Unit>
    fun close()
}