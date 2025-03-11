package com.connor.episode.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.connor.episode.domain.model.business.Owner
import com.connor.episode.domain.model.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages ORDER BY id ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE owner = :owner ORDER BY id ASC")
    fun getAllOwnerMessages(owner: Owner): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isMe = 1 AND owner = :owner ORDER BY id DESC LIMIT 1")
    fun getLastSendMessage(owner: Owner): Flow<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE owner = :owner")
    suspend fun deleteAllMessages(owner: Owner)

}