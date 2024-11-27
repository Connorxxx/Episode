package com.connor.episode.features.serial.components

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.connor.episode.domain.model.uimodel.SerialPortAction
import com.connor.episode.domain.model.uimodel.SerialPortState
import com.connor.episode.ui.common.OutlineMenu
import com.connor.episode.ui.theme.EpisodeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingDialog(
    state: SerialPortState = SerialPortState(),
    onAction: (SerialPortAction) -> Unit = {}
) {
    var localSerial by rememberSaveable(state.showSettingDialog) {
        mutableStateOf(state.model.portName.ifEmpty { state.model.serialPorts.firstOrNull()?.name ?: "" })
    }
    var localBaudRate by rememberSaveable(state.showSettingDialog) { mutableStateOf(state.model.baudRate) }

    val enable = localSerial.isNotEmpty() && localBaudRate.isNotEmpty()

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = { onAction(SerialPortAction.IsShowSettingDialog) }) {
                        Text(text = "Cancel", color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.padding(8.dp))
                    Button(
                        onClick = {
                            onAction(
                                SerialPortAction.ConfirmSetting(
                                    localSerial,
                                    localBaudRate
                                )
                            )
                        },
                        enabled = enable,
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.error,
                            disabledContentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(text = "OK")
                    }
                }
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