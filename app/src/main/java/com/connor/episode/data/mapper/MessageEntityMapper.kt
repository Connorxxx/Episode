package com.connor.episode.data.mapper

import com.connor.episode.data.local.database.entity.MessageEntity
import com.connor.episode.domain.model.Message

fun MessageEntity.toMessage() = Message(
    content = content,
    isMe = isMe,
    time = time
)