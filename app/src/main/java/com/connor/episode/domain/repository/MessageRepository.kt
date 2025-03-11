package com.connor.episode.domain.repository

import com.connor.episode.domain.model.business.Message
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.entity.MessageEntity
import kotlinx.coroutines.flow.SharedFlow

interface MessageRepository {
    val allMessagesFlow: SharedFlow<List<MessageEntity>>
    suspend fun getAllMessages(owner: Owner): List<Message>
    suspend fun getLastSendMessage(owner: Owner): MessageEntity?
    suspend fun addMessage(msg: MessageEntity)
    suspend fun deleteAllMessages(owner: Owner)
    suspend fun getLastedId(): Int
}