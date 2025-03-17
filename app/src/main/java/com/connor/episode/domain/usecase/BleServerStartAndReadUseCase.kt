package com.connor.episode.domain.usecase

import com.connor.episode.domain.repository.BleServerRepository
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BleServerStartAndReadUseCase @Inject constructor(
    private val bleServerRepository: BleServerRepository,
    private val messageRepository: MessageRepository,
    private val preferencesRepository: PreferencesRepository
) {

    operator fun invoke() = bleServerRepository.startServerAndRead(preferencesRepository::getReceiveFormat).map {
        it.onRight { message ->
            messageRepository.addMessage(message)
        }.mapLeft { err ->
            err.msg
        }.leftOrNull()
    }.filterNotNull()
}