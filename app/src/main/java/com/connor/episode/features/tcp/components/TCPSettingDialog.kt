package com.connor.episode.features.tcp.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.connor.episode.core.utils.validateIp
import com.connor.episode.core.utils.validatePort
import com.connor.episode.domain.model.business.SelectType
import com.connor.episode.domain.model.uimodel.NetAction
import com.connor.episode.domain.model.uimodel.NetAction.ConnectServer
import com.connor.episode.domain.model.uimodel.NetAction.StartServer
import com.connor.episode.domain.model.uimodel.NetAction.Top
import com.connor.episode.domain.model.uimodel.NetState
import com.connor.episode.domain.model.uimodel.TopBarAction
import com.connor.episode.features.common.ui.common.OptionButton
import com.connor.episode.features.common.ui.theme.EpisodeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TCPSettingDialog(
    state: NetState = NetState(),
    onAction: (NetAction) -> Unit = {},
) {
    var segmentSelect by remember { mutableIntStateOf(state.currentType.ordinal) }
    var serverPort by remember { mutableStateOf(state.model.server.port.toString()) }
    var clientIp by remember { mutableStateOf(state.model.client.ip) }
    var clientPort by remember { mutableStateOf(state.model.client.port.toString()) }
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
                modifier = Modifier.padding(32.dp),
            ) {
                TCPTypeSegmentedButton(
                    selectIdx = segmentSelect,
                    onSelected = { segmentSelect = it }
                )
                Spacer(modifier = Modifier.height(24.dp))
                AnimatedVisibility (segmentSelect == 1) {
                    OutlinedTextField(
                        label = { Text("IP Address") },
                        value = clientIp,
                        onValueChange = { clientIp = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(25.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    label = { Text("Port") },
                    value = when(SelectType.entries[segmentSelect]) {
                        SelectType.Server -> serverPort
                        SelectType.Client -> clientPort
                    },
                    onValueChange = {
                        when(SelectType.entries[segmentSelect]) {
                            SelectType.Server -> serverPort = it
                            SelectType.Client -> clientPort = it
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))
                OptionButton(
                    cancelText = "Cancel",
                    okText = if (segmentSelect == 0) "Start" else "Connect",
                    cancel = { onAction(Top(TopBarAction.IsShowSettingDialog(false))) },
                    ok = {
                        val check = when (SelectType.entries[segmentSelect]) {
                            SelectType.Server -> serverPort.validatePort()
                            SelectType.Client -> clientIp.validateIp() && clientPort.validatePort()
                        }
                        if (!check) return@OptionButton
                        val action = when (SelectType.entries[segmentSelect]) {
                            SelectType.Server -> StartServer(serverPort)
                            SelectType.Client -> ConnectServer(clientIp, clientPort)
                        }
                        onAction(action)
                        onAction(Top(TopBarAction.IsShowSettingDialog(false)))
                    }
                )
            }
        }
    }
}

@Composable
private fun TCPTypeSegmentedButton(
    selectIdx: Int = 0,
    onSelected: (Int) -> Unit = {}
) {
    val options = listOf("Server", "Client")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, s ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    onClick = { onSelected(index) },
                    selected = index == selectIdx
                ) {
                    Text(text = s)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DialogPreview() {
    EpisodeTheme {
        TCPSettingDialog()
    }
}