package com.connor.episode.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import com.connor.episode.data.local.database.dao.MessageDao
import com.connor.episode.data.mapper.toMessage
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.repository.MessageRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
) : MessageRepository {

    override fun getAllPagingFlow(owner: Owner) = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            initialLoadSize = 20,
            prefetchDistance = 15
        ),
        initialKey = 0,
        pagingSourceFactory = { messageDao.getAllPagingMessages(owner) }
    ).flow.map { it.map { it.toMessage() } }

    override suspend fun getLastSendMessage(owner: Owner) = messageDao.getLastSendMessage(owner)

    override suspend fun addMessage(msg: MessageEntity) {
        messageDao.insertMessage(msg)
    }

    override suspend fun deleteAllMessages(owner: Owner) {
        messageDao.deleteAllMessages(owner)
    }

}