package com.connor.episode.features.serial.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.connor.episode.domain.model.uimodel.SerialPortAction
import com.connor.episode.domain.model.uimodel.SerialPortState
import com.connor.episode.domain.model.uimodel.TopBarAction
import com.connor.episode.features.common.ui.common.OptionButton
import com.connor.episode.features.common.ui.common.OutlineMenu
import com.connor.episode.features.common.ui.theme.EpisodeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingDialog(
    state: SerialPortState = SerialPortState(),
    onAction: (SerialPortAction) -> Unit = {}
) {
    var localSerial by remember(state.showSettingDialog) {
        mutableStateOf(state.model.portName)
    }
    var localBaudRate by remember(state.showSettingDialog) { mutableStateOf(state.model.baudRate) }

    val enable = state.model.serialPorts.isNotEmpty() && localBaudRate.isNotEmpty()

    BasicAlertDialog(onDismissRequest = { }) {
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
                    text = localSerial,
                    menus = state.model.serialPorts.map { it.name },
                    onClick = { localSerial = it }
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    "baud rate: ",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = localBaudRate,
                    onValueChange = { s ->
                        localBaudRate = s.filter { it.isDigit() }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(45),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))
                OptionButton(
                    cancelText = "Cancel",
                    okText = "OK",
                    cancel = { onAction(SerialPortAction.Top(TopBarAction.IsShowSettingDialog(false))) },
                    ok = { onAction(SerialPortAction.ConfirmSetting(localSerial, localBaudRate)) }
                )
            }
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