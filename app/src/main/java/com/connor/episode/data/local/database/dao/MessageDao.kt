package com.connor.episode.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.connor.episode.domain.model.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY id DESC LIMIT 1")
    fun getLastMessage(): Flow<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

}