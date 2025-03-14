package com.connor.episode.domain.model.uimodel

import androidx.compose.ui.text.input.TextFieldValue
import com.connor.episode.domain.model.business.Message
import com.connor.episode.domain.model.business.NetModel
import com.connor.episode.domain.model.business.NetResult
import com.connor.episode.domain.model.business.SelectType
import com.connor.episode.domain.model.preference.BottomBarSettings

data class NetState(
    val model: NetModel = NetModel(),
    val bottomBarSettings: BottomBarSettings = BottomBarSettings(),
    val isShowSettingDialog: Boolean = false,
    val currentType: SelectType = SelectType.Server,
    val result: NetResult = NetResult.Close,
    val expandedBottomBar: Boolean = false,
    val message: TextFieldValue = TextFieldValue(""),
    val error: String = ""
)

sealed interface NetAction {
    data class Bottom(val bottom: BottomBarAction) : NetAction
    data class Top(val top: TopBarAction) : NetAction
    data class StartServer(val port: String) : NetAction
    data class ConnectServer(val ip: String, val port: String) : NetAction
}