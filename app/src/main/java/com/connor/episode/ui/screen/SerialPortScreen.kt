package com.connor.episode.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connor.episode.models.Message
import com.connor.episode.models.SerialPortAction
import com.connor.episode.models.SerialPortState
import com.connor.episode.ui.common.ChatMessageLazyColumn
import com.connor.episode.ui.common.MessageBottomBar
import com.connor.episode.ui.common.OutlineMenu
import com.connor.episode.ui.common.TopBar
import com.connor.episode.ui.theme.EpisodeTheme
import com.connor.episode.viewmodels.SerialPortViewModel

@Composable
fun SerialPortScreen(vm: SerialPortViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    SerialPort(state, vm::onAction)
    SettingDialog(state, vm::onAction
    )
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
    val info = state.serialPort + " : " + state.baudRate
    Scaffold(
        topBar = {
            TopBar(
                isConnecting = state.isConnected,
                connectInfo = info,
                showMenu = state.showMenu,
                onAction = onAction
            )
        },
        bottomBar = {
            BottomBar(
                msg = state.message,
                onAction = onAction
            )
        }
    ) {
        ChatMessageLazyColumn(
            modifier = Modifier.padding(bottom = it.calculateBottomPadding()),
            state.messages
        )
    }
}

@Composable
private fun BottomBar(
    msg: String = "Test message",
    onAction: (SerialPortAction) -> Unit = {}
) {
    MessageBottomBar(
        msg = msg,
        onValueChange = { onAction(SerialPortAction.WriteMsg(it)) },
        onSend = { onAction(SerialPortAction.Send) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingDialog(
    state: SerialPortState = SerialPortState(),
    onAction: (SerialPortAction) -> Unit = {}
) {
    if (!state.showSetting) return
    BasicAlertDialog(
        onDismissRequest = { })
    {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .fillMaxWidth(),
            shape = AlertDialogDefaults.shape,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "Serial port setting",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "Serial port: ",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlineMenu(
                    text = state.serialPort,
                    menus = state.serialPorts,
                    onClick = { onAction(SerialPortAction.SelectSerialPort(it)) }
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    "baud rate: ",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.baudRate,
                    onValueChange = { s ->
                        onAction(
                            SerialPortAction.ChangeBaudRate(
                                s.filter { it.isDigit() }
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(45),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = { onAction(SerialPortAction.ShowSetting) }) {
                        Text(text = "Cancel", color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.padding(8.dp))
                    Button(onClick = { onAction(SerialPortAction.ConfirmSetting) }) {
                        Text(text = "OK")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    EpisodeTheme {
        SerialPort()
    }
}

@Preview(showBackground = true)
@Composable
private fun DialogPreview() {
    EpisodeTheme {
        SettingDialog()
    }
}