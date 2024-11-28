package com.connor.episode.data.repository

import com.connor.episode.data.local.database.dao.MessageDao
import com.connor.episode.data.mapper.toMessage
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.repository.MessageRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao
) : MessageRepository {

    override suspend fun getAllMessages() =
        messageDao.getAllMessages().first().map { it.toMessage() }

    override suspend fun getLastMessage() = messageDao.getLastMessage().firstOrNull()


    override suspend fun addMessage(msg: MessageEntity) {
        messageDao.insertMessage(msg)
    }

    override suspend fun deleteAllMessages() {
        messageDao.deleteAllMessages()
    }
}