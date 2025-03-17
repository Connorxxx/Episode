package com.connor.episode.features.ble.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connor.episode.domain.model.business.SelectType
import com.connor.episode.domain.model.uimodel.AdvertisingState
import com.connor.episode.domain.model.uimodel.BleAction
import com.connor.episode.domain.model.uimodel.BleAction.Top
import com.connor.episode.domain.model.uimodel.BleState
import com.connor.episode.domain.model.uimodel.ConnectState
import com.connor.episode.domain.model.uimodel.ScanState
import com.connor.episode.domain.model.uimodel.ServerState
import com.connor.episode.domain.model.uimodel.TopBarAction
import com.connor.episode.features.common.ui.common.CenterSegmentedButton
import com.connor.episode.features.common.ui.common.StatusIndicator
import com.connor.episode.features.common.ui.theme.EpisodeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleSettingDialog(
    state: BleState = BleState(),
    onAction: (BleAction) -> Unit = {}
) {
    var segmentSelect by remember { mutableIntStateOf(state.currentType.ordinal) }

    BasicAlertDialog(
        modifier = Modifier,
        onDismissRequest = { }
    ) {
        Surface(
            modifier = Modifier
//                .wrapContentWidth()
//                .wrapContentHeight()
                .fillMaxWidth(),
            shape = AlertDialogDefaults.shape,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(32.dp)
            ) {
                CenterSegmentedButton(
                    selectIdx = segmentSelect,
                    onSelected = { segmentSelect = it }
                )
                Spacer(modifier = Modifier.height(24.dp))
                val isServer = segmentSelect == SelectType.Server.ordinal

                AnimatedContent(
                    targetState = isServer,
                    // transitionSpec = heightTransition()
                ) {
                    if (it) ServerUI(
                        advertisingState = state.advertisingState,
                        serverState = state.serverState,
                        onAction = onAction
                    ) else ClientUI(
                        deviceName = state.connectDevice.name,
                        scanState = state.scanState,
                        connectState = state.connectState,
                        devices = state.deviceNames,
                        onAction = onAction
                    )
                }
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        modifier = Modifier
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = 0.56f,
                                    stiffness = 200f
                                )
                            )
                            .fillMaxWidth(fraction = 0.55f),
                        onClick = {
                            onAction(Top(TopBarAction.IsShowSettingDialog(false)))
                        },
                        enabled = true,
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.error,
                            disabledContentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(text = "Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerUI(
    advertisingState: AdvertisingState = AdvertisingState.Running,
    serverState: ServerState = ServerState.Inactive,
    onAction: (BleAction) -> Unit = {}
) {
    val advertiseRunning = advertisingState == AdvertisingState.Running
    val serverRunning = serverState == ServerState.Active
    Column {
        BleStateCard(
            isSuccess = advertiseRunning,
            title = "Advertise",
            subtitle = if (advertiseRunning) "discoverable" else "hidden",
            isLoading = advertiseRunning,
            onClick = { onAction(BleAction.ClickAdvertise) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        BleStateCard(
            isSuccess = serverRunning,
            title = "Server",
            subtitle = if (serverRunning) "active" else "inactive",
            buttonText = "start",
            onClick = { onAction(BleAction.ClickServer) }
        )
    }
}

@Composable
private fun ClientUI(
    deviceName: String = "Device",
    scanState: ScanState = ScanState.Scanning,
    connectState: ConnectState = ConnectState.Disconnected,
    devices: Set<String> = (0..1).map { "Device $it" }.toSet(),
    onAction: (BleAction) -> Unit = {}
) {
    val deviceList = devices.toList()
    val listState = rememberLazyListState()
    LaunchedEffect(deviceList.size) {
        listState.animateScrollToItem((deviceList.size - 1).coerceAtLeast(0))
    }
    val scanning = scanState == ScanState.Scanning
    val connected = connectState == ConnectState.Connected
    Column {
        BleStateCard(
            isSuccess = scanning,
            title = "Scan",
            subtitle = if (scanning) "scanning" else "stopped",
            isLoading = scanning,
            onClick = { onAction(BleAction.ClickScan) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        BleStateCard(
            isSuccess = connected,
            title = deviceName,
            subtitle = if (connected) "connected" else "disconnected",
            buttonText = "link",
            onClick = { onAction(BleAction.ClickLink) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp, max = 220.dp)
        ) {
            Text(
                text = "Device List",
                modifier = Modifier
                    .padding(12.dp)
                    .padding(start = 12.dp),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                fontSize = 18.sp
            )
            LazyColumn(
                modifier = Modifier.padding(bottom = 12.dp),
                state = listState
            ) {
                items(items = deviceList, key = { it }) {
                    Card(
                        modifier = Modifier
                            .padding(vertical = 5.dp, horizontal = 14.dp)
                            .height(42.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = it,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onAction(BleAction.ClickConnect(it)) }) {
                                Icon(imageVector = Icons.Filled.Link, contentDescription = null)
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun BleStateCard(
    modifier: Modifier = Modifier,
    isSuccess: Boolean = true,
    isLoading: Boolean = false,
    title: String = "Advertise",
    subtitle: String = "discoverable",
    buttonText: String = "start",
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
           // horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatusIndicator(
                isSuccess = isSuccess,
                modifier = Modifier.padding(vertical = 18.dp, horizontal = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isLoading)
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(start = 6.dp, top = 2.dp)
                                .size(10.dp),
                            strokeWidth = 2.dp
                        )
                }
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
            StartOrStopButton(isSuccess = isSuccess, text = buttonText, onClick = onClick)
        }
    }
}

@Composable
private fun StartOrStopButton(
    modifier: Modifier = Modifier,
    isSuccess: Boolean = true,
    text: String = "start",
    onClick: () -> Unit = {}
) {
    AnimatedContent(isSuccess, modifier = modifier) {
        if (it) {
            IconButton(  //stop
                modifier = Modifier.padding(end = 12.dp),
                onClick = onClick
            ) {
                Icon(
                    imageVector = Icons.Outlined.StopCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                )
            }
        } else {
            FilterChip(  //start
                modifier = Modifier.padding(end = 12.dp),
                selected = true,
                onClick = onClick,
                label = { Text(text, maxLines = 1) }
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun DialogPreview() {
    EpisodeTheme {
        BleSettingDialog()
    }
}

@Preview(showBackground = true)
@Composable
private fun ClientUIPreview() {
    EpisodeTheme {

        Column(modifier = Modifier.padding(12.dp)) {
            ClientUI()
        }
    }
}