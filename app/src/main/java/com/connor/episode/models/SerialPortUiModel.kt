package com.connor.episode.models

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class SerialPortState(
    val serialPorts: List<String> = emptyList(),
    val serialPort: String = "",
    val baudRate: String = "9600",
    val isConnected: Boolean = false,  //TODO: 根据状态显示界面
    val messages: List<Message> = emptyList(),
    val message: String = "",
    val resend: Boolean = false,
    val resendTime: Duration = 1.seconds,
    val showMenu: Boolean = false,
    val showSetting: Boolean = false,
    val extraInfo: String = "",
)

sealed interface SerialPortAction {
    data object Send : SerialPortAction
    data class WriteMsg(val msg: String) : SerialPortAction
    data class IsShowMenu(val show: Boolean) : SerialPortAction
    data object CleanLog : SerialPortAction
    data object IsShowSetting : SerialPortAction
    data class SelectSerialPort(val path: String) : SerialPortAction
    data class ChangeBaudRate(val baudRate: String) : SerialPortAction
    data object ConfirmSetting : SerialPortAction
    data object Close : SerialPortAction
}