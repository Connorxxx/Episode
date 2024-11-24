package com.connor.episode.domain.model

import java.time.LocalDateTime

data class SerialPortModel(
    val serialPorts: List<String> = emptyList(),
    val serialPort: String = "",
    val baudRate: String = "9600",
    val messages: List<Message> = emptyList(),
    val resend: Boolean = false,
    val resendSeconds: Int = 1,
    val sendFormat: Int = 0,  //0 HEX 1 ASCII
    val receiveFormat: Int = 0,
)

data class Message(
    val content: String = "",
    val isMe: Boolean = false,
    val time: LocalDateTime = LocalDateTime.now()
)