package com.connor.episode.domain.model.uimodel

import androidx.compose.ui.text.input.TextFieldValue
import com.connor.episode.domain.model.business.Message
import com.connor.episode.domain.model.business.SerialPortModel
import com.connor.episode.domain.model.business.SerialPortSettings

data class SerialPortState(
    val model: SerialPortModel = SerialPortModel(),
    val messages: List<Message> = emptyList(),
    val settings: SerialPortSettings = SerialPortSettings(),
    val extraInfo: String = "Close",
    val showSettingDialog: Boolean = false,
    val isConnected: Boolean = false,  //TODO: 根据状态显示界面
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
    data class Resend(val resend: Boolean) : SerialPortAction
    data class ResendSeconds(val seconds: Int) : SerialPortAction
    data class OnMessageChange(val msg: TextFieldValue) : SerialPortAction
}

data class SerialPortUi(
    val options: List<String> = listOf("HEX", "ASCII")
)