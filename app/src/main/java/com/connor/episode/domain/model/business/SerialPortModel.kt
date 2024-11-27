package com.connor.episode.domain.model.business

import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDateTime

//data class SerialPortModel(
//    val serialPorts: List<String> = emptyList(),
//    val serialPort: String = "",
//    val baudRate: String = "9600",
//    val messages: List<Message> = emptyList(),
//    val resend: Boolean = false,
//    val resendSeconds: Int = 1,
//    val sendFormat: Int = 0,  //0 HEX 1 ASCII
//    val receiveFormat: Int = 0,
//)

data class Message(
    val content: String = "",
    val isMe: Boolean = false,
    val time: LocalDateTime = LocalDateTime.now()  //TODO: Add sendState and sendType
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

@Serializable
data class SerialPortSettings(
    val resend: Boolean = false,
    val resendSeconds: Int = 1,
    val sendFormat: Int = 0,
    val receiveFormat: Int = 0,
)

sealed interface SerialMessage {
    data class Received(
        val data: ByteArray,
        val timestamp: LocalDateTime = LocalDateTime.now(),
        val format: DataFormat = DataFormat.HEX
    ) : SerialMessage

    data class Sent(
        val data: ByteArray,
        val timestamp: LocalDateTime = LocalDateTime.now(),
        val format: DataFormat = DataFormat.HEX
    ) : SerialMessage

    enum class DataFormat { HEX, ASCII, UTF8 }
}

fun test() {
    val bytes:List<Byte> = listOf(0x01, 0x02, 0x03)
    fun sed(data: ByteArray) {

    }
    sed(bytes.toByteArray())
}