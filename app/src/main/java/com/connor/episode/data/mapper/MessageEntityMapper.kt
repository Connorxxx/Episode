package com.connor.episode.data.mapper

import com.connor.episode.domain.model.business.Message
import com.connor.episode.domain.model.entity.MessageEntity

fun MessageEntity.toMessage() = Message(
    id = id,
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
    type = type,
    owner = owner
)