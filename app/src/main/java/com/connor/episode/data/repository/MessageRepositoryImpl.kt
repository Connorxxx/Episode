package com.connor.episode.data.repository

import com.connor.episode.data.local.database.dao.MessageDao
import com.connor.episode.data.mapper.toMessage
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.repository.MessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    appScope: CoroutineScope
) : MessageRepository {

    override val allMessagesFlow = messageDao.getAllMessages().shareIn(
        appScope,
        SharingStarted.Lazily,
        replay = 1
    )

    override suspend fun getAllMessages(owner: Owner) = allMessagesFlow.first()
        .filter { it.owner == owner }
        .map { it.toMessage() }

    override suspend fun getLastSendMessage(owner: Owner) =
        allMessagesFlow.first().lastOrNull { it.owner == owner && it.isMe }

    override suspend fun addMessage(msg: MessageEntity) {
        messageDao.insertMessage(msg)
    }

    override suspend fun deleteAllMessages(owner: Owner) {
        messageDao.deleteAllMessages(owner)
    }

    override suspend fun getLastedId() = allMessagesFlow.first().lastOrNull()?.id ?: 0
}