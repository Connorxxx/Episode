package com.connor.episode.models

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
    val sendFormatIdx: Int = 0,
    val receiveFormatIdx: Int = 0
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
}