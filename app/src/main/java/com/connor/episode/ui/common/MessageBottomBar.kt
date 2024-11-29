package com.connor.episode.ui.common

import OutlinedTextField
import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.connor.episode.core.utils.logCat
import com.connor.episode.domain.model.preference.BottomBarSettings
import com.connor.episode.domain.model.uimodel.BottomBarAction
import com.connor.episode.ui.state.rememberKeyboardState
import com.connor.episode.ui.theme.EpisodeTheme
import kotlinx.coroutines.delay

@Composable
fun MessageBottomBar(
    enabled: Boolean = true,
    expanded: Boolean = false,
    options: List<String> = listOf("HEX", "ASCII"),
    message: TextFieldValue = TextFieldValue(""),
    state: BottomBarSettings = BottomBarSettings(),
    onAction: (BottomBarAction) -> Unit = {}
) {
    val isKeyboardVisible by rememberKeyboardState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    LaunchedEffect(isKeyboardVisible) {
        when (isKeyboardVisible) {
            true -> onAction(BottomBarAction.Expand(false))
            false -> focusManager.clearFocus()
        }
    }
    LaunchedEffect(expanded) {
        if (expanded) {
            val imm = context.getSystemService<InputMethodManager>()!!
            (context as Activity).currentFocus?.let {
                imm.hideSoftInputFromWindow(it.windowToken, 0)
            }
        }
    }
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
            IconButton(onClick = { onAction(BottomBarAction.Expand(!expanded)) }) {
                Icon(
                    if (!expanded) Icons.Filled.GridView else Icons.Rounded.GridView,
                    contentDescription = null
                )
            }
            OutlinedTextField(
                value = message,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        isFocused = it.isFocused
                    },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface
                ),
                onValueChange = { onAction(BottomBarAction.OnMessageChange(it)) },
                //shape = RoundedCornerShape(6.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                placeholder = {
                    Text(
                        text = "Write a ${if (state.sendFormat == 0) "HEX" else "ASCII"} message...",
                        style = TextStyle(
                            color = Color.Gray,
                        )
                    )
                },
                suffix = {
                    if (message.text.isNotEmpty())
                        Icon(
                            modifier = Modifier.clickable(
                                onClick = { onAction(BottomBarAction.OnMessageChange(TextFieldValue())) }
                            ), imageVector = Icons.Filled.Close, contentDescription = null)
                },
                keyboardActions = KeyboardActions(
                    onSend = { onAction(BottomBarAction.Send(message.text)) }
                ),
                maxLines = 4,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            )
            //PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            IconButton(
                onClick = { onAction(BottomBarAction.Send(message.text)) },
                enabled = enabled,
                modifier = Modifier
                    .padding(start = 6.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        }
        if (expanded) {
            Column {
                FormatType(
                    text = "Send format:",
                    options,
                    state.sendFormat,
                    onSelected = { onAction(BottomBarAction.SendFormatSelect(it)) }
                )
                Spacer(modifier = Modifier.padding(4.dp))
                FormatType(
                    text = "Receive format:",
                    options,
                    state.receiveFormat,
                    onSelected = { onAction(BottomBarAction.ReceiveFormatSelect(it)) }
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .animateContentSize(),
                        enabled = enabled,
                        selected = state.resend,
                        onClick = { onAction(BottomBarAction.Resend(!state.resend)) },
                        label = { Text("Resend", maxLines = 1) },
                        leadingIcon =
                            if (state.resend) {
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
                    AnimatedVisibility(visible = state.resend) {
                        NumberPicker(
                            value = state.resendSeconds,
                            onValueChange = { onAction(BottomBarAction.ResendSeconds(it)) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                }
                Spacer(modifier = Modifier.padding(4.dp))
            }
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