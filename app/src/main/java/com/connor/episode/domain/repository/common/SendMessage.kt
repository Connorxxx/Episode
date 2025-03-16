package com.connor.episode.domain.repository.common

import arrow.core.Either
import com.connor.episode.domain.model.error.EpisodeError

interface SendMessage<E: EpisodeError> {

    suspend fun sendMessage(msg: String, msgType: Int): Either<E, Unit>

}