package com.connor.episode.domain.repository

import com.connor.episode.domain.model.business.Message

interface MessageRepository {
    suspend fun getAllMessages(): List<Message>
    suspend fun addMessage(msg: Message)
    suspend fun deleteAllMessages()
}