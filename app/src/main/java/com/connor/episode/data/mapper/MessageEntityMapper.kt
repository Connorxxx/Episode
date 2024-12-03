package com.connor.episode.data.mapper

import com.connor.episode.domain.model.entity.MessageEntity
import com.connor.episode.domain.model.business.Message

fun MessageEntity.toMessage() = Message(
    name = name,
    content = content,
    isMe = isMe,
    time = time,
    sendSuccessful = sendSuccessful,
    type = type
)

fun Message.toEntity() = MessageEntity(
    name = name,
    content = content,
    isMe = isMe,
    time = time,
    sendSuccessful = sendSuccessful,
    type = type
)