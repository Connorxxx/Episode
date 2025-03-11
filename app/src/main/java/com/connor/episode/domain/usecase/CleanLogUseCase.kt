package com.connor.episode.domain.usecase

import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CleanLogUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {

    suspend operator fun invoke(owner: Owner) = withContext(Dispatchers.IO){
        messageRepository.deleteAllMessages(owner)
    }
}