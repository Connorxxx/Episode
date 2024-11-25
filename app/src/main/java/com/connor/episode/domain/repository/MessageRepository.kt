package com.connor.episode.domain.repository

import com.connor.episode.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun getAllMessages(): List<Message>
    suspend fun addMessage(msg: Message)
    suspend fun deleteAllMessages()
}