package com.connor.episode.domain.repository

import arrow.core.Either
import com.connor.episode.domain.config.SerialConfig
import com.connor.episode.domain.error.SerialPortError
import kotlinx.coroutines.flow.Flow

interface SerialPortRepository {
    val getAllDevices: List<String>
    fun openAndRead(config: SerialConfig.() -> Unit): Flow<Either<SerialPortError, ByteArray>>
    fun write(data: ByteArray): Either<SerialPortError, Unit>
    fun close()
}