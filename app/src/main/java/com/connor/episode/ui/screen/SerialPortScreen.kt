package com.connor.episode.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.connor.episode.models.SerialPortAction
import com.connor.episode.ui.common.ChatMessageLazyColumn
import com.connor.episode.ui.common.MessageBottomBar
import com.connor.episode.ui.common.OutlineMenu
import com.connor.episode.ui.theme.EpisodeTheme
import com.connor.episode.viewmodels.SerialPortViewModel

@Composable
fun SerialPortScreen(vm: SerialPortViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    SerialPort(
        message = state.message,
        showMenu = state.showMenu,
        onAction = vm::onAction
    ) {
        ChatMessageLazyColumn(
            modifier = Modifier.padding(bottom = it.calculateBottomPadding()),
            state.messages
        )
    }
    SettingDialog(
        show = state.showSetting,
        currentSerialPort = state.serialPort,
        serialPortMenus = state.serialPorts,
        budaRate = state.baudRate,
        onAction = vm::onAction
    )
}

@Composable
private fun SerialPort(
    message: String = "",
    showMenu: Boolean = false,
    onAction: (SerialPortAction) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = { TopBar(onAction = onAction, showMenu = showMenu) },
        bottomBar = { BottomBar(msg = message, onAction = onAction) }
    ) {
        content(it)
    }
}

@Composable
private fun TopBar(
    onAction: (SerialPortAction) -> Unit = {},
    showMenu: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Column {
            IconButton(onClick = { onAction(SerialPortAction.ShowMenu) }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { onAction(SerialPortAction.ShowMenu) }) {
                DropdownMenuItem(
                    text = { Text(text = "Setting") },
                    onClick = { onAction(SerialPortAction.ShowSetting) },
                    leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text(text = "Clean log") },
                    onClick = { onAction(SerialPortAction.CleanLog) },
                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) }
                )
            }
        }
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
    show: Boolean = true,
    currentSerialPort: String = "",
    budaRate: String = "9600",
    serialPortMenus: List<String> = emptyList(),
    onAction: (SerialPortAction) -> Unit = {}
) {
    if (!show) return
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
                    text = currentSerialPort,
                    menus = serialPortMenus,
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
                    value = budaRate,
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
        SerialPort {
            ChatMessageLazyColumn()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DialogPreview() {
    EpisodeTheme {
        SettingDialog()
    }
}