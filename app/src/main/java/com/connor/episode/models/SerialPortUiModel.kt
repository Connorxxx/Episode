package com.connor.episode.models

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class SerialPortState(
    val serialPorts: List<String> = emptyList(),
    val serialPort: String = "",
    val baudRate: String = "9600",
    val isConnected: Boolean = false,  //TODO: 根据状态显示界面
    val messages: List<Message> = emptyList(),
    val resend: Boolean = false,
    val resendTime: Duration = 1.seconds,
    val showSettingDialog: Boolean = false,
    val extraInfo: String = "Close",
)

sealed interface SerialPortAction {
    data class Send(val msg: String) : SerialPortAction
    data object CleanLog : SerialPortAction
    data object IsShowSettingDialog : SerialPortAction
    data class ConfirmSetting(val serialPort: String, val baudRate: String) : SerialPortAction
    data object Close : SerialPortAction
}