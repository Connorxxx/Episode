package com.connor.episode.features.serial

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connor.episode.domain.model.Message
import com.connor.episode.features.serial.components.SettingDialog
import com.connor.episode.ui.common.ChatMessageLazyColumn
import com.connor.episode.ui.common.MessageBottomBar
import com.connor.episode.ui.common.TopBar
import com.connor.episode.ui.theme.EpisodeTheme

@Composable
fun SerialPortScreen(vm: SerialPortViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    SerialPort(state, vm::onAction)

    if (state.showSettingDialog)
        SettingDialog(state, vm::onAction)

}

@Composable
private fun SerialPort(
    state: SerialPortState = SerialPortState(
        serialPort = "ttyS0",
        isConnected = true,
        messages = (0..5).map { Message(it.toString(), it % 2 == 0) }
    ),
    onAction: (SerialPortAction) -> Unit = {}
) {
    val connectInfo = if (state.isConnected) "${state.serialPort} : ${state.baudRate}" else ""
    val info = "$connectInfo  ${state.extraInfo}"
    Scaffold(
        topBar = {
            TopBar(
                isConnecting = state.isConnected,
                connectInfo = info,
                onAction = onAction
            )
        },
        bottomBar = {
            MessageBottomBar(
                sendSelectIdx = state.sendFormatIdx,
                receiveSelectIdx = state.receiveFormatIdx,
                isResend = state.resend,
                resendSeconds = state.resendSeconds,
                message = state.message,
                onSendMessage = { onAction(SerialPortAction.Send(it)) },
                onSendFormatSelect = { onAction(SerialPortAction.SendFormatSelect(it)) },
                onReceiveFormatSelect = { onAction(SerialPortAction.ReceiveFormatSelect(it)) },
                onResend = { onAction(SerialPortAction.Resend) },
                onResendSeconds = { onAction(SerialPortAction.ResendSeconds(it)) },
                onMessageChange = { onAction(SerialPortAction.OnMessageChange(it)) }
            )
        }
    ) {
        ChatMessageLazyColumn(
            modifier = Modifier.padding(it),
            state.messages
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    EpisodeTheme {
        SerialPort()
    }
}

