package com.connor.episode.features.serial

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connor.episode.domain.model.business.Message
import com.connor.episode.domain.model.business.SerialPortModel
import com.connor.episode.domain.model.uimodel.BottomBarAction
import com.connor.episode.domain.model.uimodel.SerialPortAction
import com.connor.episode.domain.model.uimodel.SerialPortState
import com.connor.episode.features.serial.components.SettingDialog
import com.connor.episode.ui.common.ChatMessageLazyColumn
import com.connor.episode.ui.common.MessageBottomBar
import com.connor.episode.ui.common.TopBar
import com.connor.episode.ui.theme.EpisodeTheme

@Composable
fun SerialPortScreen(vm: SerialPortViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    BackHandler(enabled = state.expandedBottomBar) {
        vm.onAction(SerialPortAction.Bottom(BottomBarAction.Expand(false)))
    }
    SerialPort(state, vm::onAction)
    if (state.showSettingDialog)
        SettingDialog(state, vm::onAction)
}

@Composable
private fun SerialPort(
    state: SerialPortState = SerialPortState(
        model = SerialPortModel(portName = "ttyS0"),
        isConnected = true,
        messages = (0..5).map { Message(it.toString(), it % 2 == 0) }
    ),
    onAction: (SerialPortAction) -> Unit = {}
) {
    val connectInfo = if (state.isConnected) "${state.model.portName} : ${state.model.baudRate}" else ""
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
                enabled = state.isConnected,
                expanded = state.expandedBottomBar,
                state = state.bottomBarSettings,
                message = state.message,
                onAction = { onAction(SerialPortAction.Bottom(it)) },
            )
        }
    ) {
        ChatMessageLazyColumn(
            modifier = Modifier
                .padding(it)
               // .then(if (state.expandedBottomBar) Modifier.padding(bottom = 100.dp) else Modifier)
                .fillMaxSize(),
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

