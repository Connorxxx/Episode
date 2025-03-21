package com.connor.episode.features.common.ui.common

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun OptionButton(
    modifier: Modifier = Modifier,
    cancelText: String = "Cancel",
    okText: String = "OK",
    cancel: () -> Unit = {},
    ok: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.End
    ) {
        OutlinedButton(onClick = cancel) {
            Text(
                text = cancelText,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.padding(8.dp))
        Button(
            modifier = Modifier.animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.56f,
                    stiffness = 200f
                )
            ),
            onClick = ok,
            enabled = true,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.error,
                disabledContentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Text(text = okText)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CenterSegmentedButton(
    options: List<String> = listOf("Server", "Client"),
    selectIdx: Int = 0,
    onSelected: (Int) -> Unit = {}
) {
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