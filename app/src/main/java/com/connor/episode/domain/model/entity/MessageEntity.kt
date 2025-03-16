package com.connor.episode.domain.model.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.connor.episode.domain.model.business.Owner
import java.time.LocalDateTime

@Entity(
    tableName = "messages",
    indices = [Index(value = ["owner"])]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val content: String = "",
    //val bytes: ByteArray = byteArrayOf(), //删除bytes列
    val isMe: Boolean = false,
    val time: LocalDateTime = LocalDateTime.now(),
    val sendSuccessful: Boolean = false,
    val type: String = "HEX",
    val owner: Owner = Owner.SerialPort
)
