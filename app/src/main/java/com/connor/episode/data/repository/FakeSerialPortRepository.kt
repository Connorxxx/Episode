package com.connor.episode.data.repository

import arrow.core.Either
import arrow.core.right
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.business.SerialPortDevice
import com.connor.episode.domain.model.config.SerialConfig
import com.connor.episode.domain.model.error.SerialPortError
import com.connor.episode.domain.repository.SerialPortRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FakeSerialPortRepository @Inject constructor() : SerialPortRepository {

    private val state = MutableSharedFlow<String>()

    override val getAllDevices = listOf("dev/ttyS0", "dev/ttyS1", "dev/ttyS2", "dev/ttyS3").map {
        SerialPortDevice(name = it.substringAfterLast("/"), path = it)
    }

    override fun openAndRead(config: SerialConfig,typeProvider: suspend (Owner) -> Int): Flow<Either<SerialPortError, String>> = state.map {
        delay(850)
        it.right()
    }

    override suspend fun sendMessage(data: String, msgType: Int): Either<SerialPortError, Unit> {
        state.emit(data)
        return Unit.right()
    }


    override suspend fun close() {

    }

}