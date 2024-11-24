package com.connor.episode.features.serial

import androidx.compose.ui.text.input.TextFieldValue
import com.connor.episode.domain.model.Message
import java.io.InputStream
import java.io.OutputStream

data class SerialPortState(
    val serialPorts: List<String> = emptyList(),
    val serialPort: String = "",
    val baudRate: String = "9600",
    val isConnected: Boolean = false,  //TODO: 根据状态显示界面
    val messages: List<Message> = emptyList(),
    val resend: Boolean = false,
    val resendSeconds: Int = 1,
    val showSettingDialog: Boolean = false,
    val extraInfo: String = "Close",
    val sendFormat: Int = 0,
    val receiveFormat: Int = 0,
    val message: TextFieldValue = TextFieldValue("")
)

sealed interface SerialPortAction {
    data class Send(val msg: String) : SerialPortAction
    data object CleanLog : SerialPortAction
    data object IsShowSettingDialog : SerialPortAction
    data class ConfirmSetting(val serialPort: String, val baudRate: String) : SerialPortAction
    data object Close : SerialPortAction
    data class SendFormatSelect(val idx: Int) : SerialPortAction
    data class ReceiveFormatSelect(val idx: Int) : SerialPortAction
    data object Resend : SerialPortAction
    data class ResendSeconds(val seconds: Int) : SerialPortAction
    data class OnMessageChange(val msg: TextFieldValue) : SerialPortAction
}

data class Person(
    val name: String,
    val age: Int,
)

sealed interface Serializer<T> {
    val defaultValue: T
    suspend fun readFrom(input: InputStream): T
    suspend fun writeTo(t: T, output: OutputStream)

    data class Json<T>(override val defaultValue: T) : Serializer<T> {
        override suspend fun readFrom(input: InputStream): T {
            TODO("Not yet implemented")
        }

        override suspend fun writeTo(t: T, output: OutputStream) {
            TODO("Not yet implemented")
        }
    }

    data class Protobuf<T>(override val defaultValue: T) : Serializer<T> {
        override suspend fun readFrom(input: InputStream): T {
            TODO("Not yet implemented")
        }
        override suspend fun writeTo(t: T, output: OutputStream) {
            TODO("Not yet implemented")
        }
    }
}