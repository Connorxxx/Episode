package com.connor.episode.data.repository

import com.connor.episode.data.local.database.dao.MessageDao
import com.connor.episode.data.mapper.toMessage
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.repository.MessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    appScope: CoroutineScope
) : MessageRepository {

    private val allMessagesFlow = messageDao.getAllMessages().shareIn(
        appScope,
        SharingStarted.Lazily,
        replay = 1
    )

    private val lastMessageFlow = messageDao.getLastSendMessage().shareIn(
        appScope,
        SharingStarted.Lazily,
        replay = 1
    )

    override suspend fun getAllMessages() = allMessagesFlow.first().map { it.toMessage() }

    override suspend fun getLastSendMessage() = lastMessageFlow.firstOrNull()

    override suspend fun addMessage(msg: MessageEntity) {
        messageDao.insertMessage(msg)
    }

    override suspend fun deleteAllMessages() {
        messageDao.deleteAllMessages()
    }
}

class MyScope @Inject constructor(appScope: CoroutineScope): CoroutineScope by appScope {

}