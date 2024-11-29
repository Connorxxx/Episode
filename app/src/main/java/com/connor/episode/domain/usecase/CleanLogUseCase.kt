package com.connor.episode.domain.usecase

import com.connor.episode.domain.repository.MessageRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CleanLogUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {

    suspend operator fun invoke() {
        messageRepository.deleteAllMessages()
    }
}