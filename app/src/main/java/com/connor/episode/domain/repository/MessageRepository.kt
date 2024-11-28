package com.connor.episode.domain.repository

import com.connor.episode.domain.model.business.Message
import com.connor.episode.domain.model.entity.MessageEntity

interface MessageRepository {
    suspend fun getAllMessages(): List<Message>
    suspend fun getLastMessage(): MessageEntity?
    suspend fun addMessage(msg: MessageEntity)
    suspend fun deleteAllMessages()
}