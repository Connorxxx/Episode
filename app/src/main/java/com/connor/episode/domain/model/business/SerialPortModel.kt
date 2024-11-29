package com.connor.episode.domain.model.business

import java.time.LocalDateTime

data class Message(
    val content: String = "",
    val isMe: Boolean = false,
    val time: LocalDateTime = LocalDateTime.now(),  //TODO: Add sendState and sendType
    val sendSuccessful: Boolean = false,
    val type: String = "HEX"
)

data class SerialPortModel(
    val serialPorts: List<SerialPortDevice> = emptyList(),
    val portName: String = "",
    val baudRate: String = "",
)

data class SerialPortDevice(
    val name: String = "",
    val path: String = ""
)