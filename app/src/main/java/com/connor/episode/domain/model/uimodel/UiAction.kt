package com.connor.episode.domain.model.uimodel

import androidx.compose.ui.text.input.TextFieldValue

sealed interface BottomBarAction {
    data class Send(val msg: String) : BottomBarAction
    data class SendFormatSelect(val idx: Int) : BottomBarAction
    data class ReceiveFormatSelect(val idx: Int) : BottomBarAction
    data class Resend(val resend: Boolean) : BottomBarAction
    data class ResendSeconds(val seconds: Int) : BottomBarAction
    data class OnMessageChange(val msg: TextFieldValue) : BottomBarAction
    data class Expand(val expand: Boolean) : BottomBarAction
}

sealed interface TopBarAction {
    data class IsShowSettingDialog(val show: Boolean) : TopBarAction
    data object CleanLog : TopBarAction
    data object Close : TopBarAction
}