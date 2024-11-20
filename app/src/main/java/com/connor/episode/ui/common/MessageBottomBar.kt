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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewCozy
import androidx.compose.material.icons.outlined.ViewCozy
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.ViewCozy
import androidx.compose.material.icons.sharp.ViewCozy
import androidx.compose.material.icons.twotone.ViewCozy
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
    // msg: String = "",
    // onValueChange: (String) -> Unit = {},
    onSend: (String) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(true) }
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
                    onSend = { onSend(msg) }
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            )
            IconButton(
                onClick = { onSend(msg) },
                modifier = Modifier
                    .padding(start = 6.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        }
        if (expanded) {
            Column {
                var selectedIndex by remember { mutableIntStateOf(0) }
                val options = listOf("HEX", "ASCII")
                var selected by remember { mutableStateOf(false) }

                //  Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Send format:",
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
                                onClick = { selectedIndex = index },
                                label = { Text(option, maxLines = 1) }
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        modifier = Modifier.padding(horizontal = 8.dp).animateContentSize(),
                        selected = selected,
                        onClick = { selected = !selected },
                        label = { Text("Resend", maxLines = 1) },
                        leadingIcon =
                            if (selected) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Done,
                                        contentDescription = "Localized Description",
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null
                    )
                    var count by remember { mutableIntStateOf(0) }
                    Spacer(modifier = Modifier.weight(1f))
                    AnimatedVisibility(visible = selected) {
                        NumberPicker(
                            value = count,
                            onValueChange = { count = it },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                }
            }
            // Spacer(modifier = Modifier.height(300.dp))
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