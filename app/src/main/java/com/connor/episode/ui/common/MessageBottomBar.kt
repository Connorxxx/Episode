package com.connor.episode.ui.common

import OutlinedTextField
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewCozy
import androidx.compose.material.icons.outlined.ViewCozy
import androidx.compose.material.icons.rounded.ViewCozy
import androidx.compose.material.icons.sharp.ViewCozy
import androidx.compose.material.icons.twotone.ViewCozy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    msg: String = "",
    onValueChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .imePadding()
            .animateContentSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(Icons.Filled.GridView, contentDescription = null)
            }
            OutlinedTextField(
                value = msg,
                modifier = Modifier.weight(1f),
                onValueChange = onValueChange,
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
                    onSend = { onSend() }
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            )
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .padding(start = 6.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        }
        if (expanded) Spacer(modifier = Modifier.height(300.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    EpisodeTheme {
        MessageBottomBar()
    }
}