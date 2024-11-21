package com.connor.episode.ui.common

import OutlinedTextField
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.connor.episode.ui.theme.EpisodeTheme

@Composable
fun MessageBottomBar(
    sendSelectIdx: Int = 0,
    receiveSelectIdx: Int = 0,
    isResend: Boolean = false,
    resendSeconds: Int = 1,
    onSendMessage: (String) -> Unit = {},
    onSendFormatSelect: (Int) -> Unit = {},
    onReceiveFormatSelect: (Int) -> Unit = {},
    onResend: () -> Unit = {},
    onResendSeconds: (Int) -> Unit = {}

) {
    var expanded by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .imePadding()
            // .then(if (expanded) Modifier.statusBarsPadding() else Modifier)
            .animateContentSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    if (!expanded) Icons.Filled.GridView else Icons.Rounded.GridView,
                    contentDescription = null
                )
            }
            OutlinedTextField(
                value = msg,
                modifier = Modifier.weight(1f),
                onValueChange = { msg = it },
                shape = RoundedCornerShape(45),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                placeholder = {
                    Text(
                        text = "write a message...", style = TextStyle(
                            color = Color.Gray,
                        )
                    )
                },
                keyboardActions = KeyboardActions(
                    onSend = { onSendMessage(msg) }
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            )
            IconButton(
                onClick = { onSendMessage(msg) },
                modifier = Modifier
                    .padding(start = 6.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        }
        if (expanded) {
            Column {
                val options = listOf("HEX", "ASCII")
                FormatType(
                    text = "Send format:",
                    options,
                    sendSelectIdx,
                    onSelected = { onSendFormatSelect(it) }
                )
                Spacer(modifier = Modifier.padding(4.dp))
                FormatType(
                    text = "Receive format:",
                    options,
                    receiveSelectIdx,
                    onSelected = { onReceiveFormatSelect(it) }
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        modifier = Modifier.padding(horizontal = 8.dp).animateContentSize(),
                        selected = isResend,
                        onClick = onResend,
                        label = { Text("Resend", maxLines = 1) },
                        leadingIcon =
                            if (isResend) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Done,
                                        contentDescription = "Localized Description",
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    AnimatedVisibility(visible = isResend) {
                        NumberPicker(
                            value = resendSeconds,
                            onValueChange = { onResendSeconds(it) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                }
                Spacer(modifier = Modifier.padding(4.dp))
            }
            // Spacer(modifier = Modifier.height(300.dp))
        }
    }
}

@Composable
private fun FormatType(
    text: String = "Send format:",
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            options.forEachIndexed { index, option ->
                FilterChip(
                    selected = index == selectedIndex,
                    onClick = { onSelected(index) },
                    label = { Text(option, maxLines = 1) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    EpisodeTheme {
        MessageBottomBar()
    }
}