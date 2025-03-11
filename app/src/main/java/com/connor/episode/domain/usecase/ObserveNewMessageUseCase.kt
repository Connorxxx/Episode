package com.connor.episode.domain.usecase

import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.repository.MessageRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

class ObserveNewMessageUseCase @Inject constructor(
    val messageRepository: MessageRepository
) {

    operator fun invoke(owner: Owner) = messageRepository.allMessagesFlow
        .mapNotNull { it.lastOrNull { it.owner == owner } }.distinctUntilChanged{ old, new -> old.id == new.id }.drop(1)
}