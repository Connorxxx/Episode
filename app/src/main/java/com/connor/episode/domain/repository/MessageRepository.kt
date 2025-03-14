package com.connor.episode.domain.repository

import androidx.paging.PagingData
import com.connor.episode.domain.model.business.Message
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getAllPagingFlow(owner: Owner): Flow<PagingData<Message>>
    suspend fun getLastSendMessage(owner: Owner): MessageEntity?
    suspend fun addMessage(msg: MessageEntity)
    suspend fun deleteAllMessages(owner: Owner)
}