package com.connor.episode.domain.repository

import arrow.core.Either
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.SerialPortDevice
import com.connor.episode.domain.model.config.SerialConfig
import com.connor.episode.domain.model.error.SerialPortError
import com.connor.episode.domain.repository.common.Close
import com.connor.episode.domain.repository.common.SendMessage
import kotlinx.coroutines.flow.Flow

interface SerialPortRepository : SendMessage<SerialPortError>, Close {
    val getAllDevices: List<SerialPortDevice>
    fun openAndRead(config: SerialConfig, typeProvider: suspend (Owner) -> Int): Flow<Either<SerialPortError, String>>
    suspend fun write(data: String, msgType: Int): Either<SerialPortError, Unit> = sendMessage(data, msgType)
}