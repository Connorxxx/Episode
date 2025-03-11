package com.connor.episode.domain.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.connor.episode.domain.model.business.Owner
import java.time.LocalDateTime

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val content: String = "",
    val bytes: ByteArray = byteArrayOf(),
    val isMe: Boolean = false,
    val time: LocalDateTime = LocalDateTime.now(),
    val sendSuccessful: Boolean = false,
    val type: String = "HEX",
    val owner: Owner = Owner.SerialPort
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageEntity

        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}
