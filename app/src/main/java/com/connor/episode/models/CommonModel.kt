package com.connor.episode.models

import java.time.LocalDateTime

data class Message(
    val content: String = "",
    val isMe: Boolean = false,
    val time: LocalDateTime = LocalDateTime.now()
)