package com.connor.episode.domain.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val isMe: Boolean,
    val time: LocalDateTime = LocalDateTime.now()
)
