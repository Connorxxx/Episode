package com.connor.episode.domain.usecase

import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.repository.MessageRepository
import javax.inject.Inject

class GetPagingMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {

    operator fun invoke(owner: Owner) = messageRepository.getAllPagingFlow(owner)
}