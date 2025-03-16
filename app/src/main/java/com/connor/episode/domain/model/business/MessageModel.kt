package com.connor.episode.domain.model.business

import java.time.LocalDateTime

data class Message(
    val id: Int = 0,
    val name: String = "",
    val content: String = "",
    val isMe: Boolean = false,
    val time: LocalDateTime = LocalDateTime.now(),  //TODO: Add sendState and sendType
    val sendSuccessful: Boolean = false,
    val type: String = "HEX",
    val owner: Owner = Owner.SerialPort,
)

enum class Owner {
    SerialPort, TCP, UDP, WebSocket, BLE
}

val msgType: List<String> = listOf("HEX", "ASCII")