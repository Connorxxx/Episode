package com.connor.episode.domain.model.uimodel

import androidx.compose.ui.text.input.TextFieldValue
import com.connor.episode.domain.model.business.Message
import com.connor.episode.domain.model.business.SerialPortModel
import com.connor.episode.domain.model.preference.BottomBarSettings

data class SerialPortState(
    val model: SerialPortModel = SerialPortModel(),
    val messages: List<Message> = emptyList(),
    val bottomBarSettings: BottomBarSettings = BottomBarSettings(),
    val extraInfo: String = "Close",
    val showSettingDialog: Boolean = false,
    val isConnected: Boolean = false,  //TODO: 根据状态显示界面
    val message: TextFieldValue = TextFieldValue(""),
    val expandedBottomBar: Boolean = false,
)

sealed interface SerialPortAction {
    data class ConfirmSetting(val serialPort: String, val baudRate: String) : SerialPortAction
    data class Bottom(val bottom: BottomBarAction) : SerialPortAction
    data class Top(val top: TopBarAction) : SerialPortAction
}

//data class SerialPortUi(
//    val options: List<String> = listOf("HEX", "ASCII")
//)