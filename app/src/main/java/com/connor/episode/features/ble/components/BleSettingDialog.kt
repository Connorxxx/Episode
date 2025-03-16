package com.connor.episode.features.ble.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Cancel
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connor.episode.domain.model.uimodel.BleAction
import com.connor.episode.domain.model.uimodel.BleAction.Top
import com.connor.episode.domain.model.uimodel.BleState
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
                CenterSegmentedButton(
                    selectIdx = segmentSelect,
                    onSelected = { segmentSelect = it }
                )
                Spacer(modifier = Modifier.height(24.dp))
                BleStateCard()
                Spacer(modifier = Modifier.height(12.dp))
                BleStateCard(
                    isSuccess = false,
                    title = "Server",
                    subtitle = "stopped"
                )
                Spacer(modifier = Modifier.height(32.dp))
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
                            onAction(BleAction.UpdateSelectType(segmentSelect))
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
private fun BleStateCard(
    modifier: Modifier = Modifier,
    isSuccess: Boolean = true,
    title: String = "Advertising",
    subtitle: String = "discoverable",
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            StatusIndicator(
                isSuccess = isSuccess,
                modifier = Modifier.padding(vertical = 18.dp, horizontal = 12.dp)
            )
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(visible = isSuccess) {
                Icon(
                    imageVector = Icons.Sharp.Cancel,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
            AnimatedVisibility(visible = !isSuccess) {
                FilterChip(
                    modifier = Modifier.padding(end = 12.dp),
                    selected = true,
                    onClick = { },
                    label = { Text("start", maxLines = 1) }
                )
            }
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