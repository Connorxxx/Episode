package com.connor.episode.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.connor.episode.models.SerialPortAction

@Composable
fun TopBar(
    isConnecting: Boolean = false,
    connectInfo: String = "ttyS0 : 9600",
    onAction: (SerialPortAction) -> Unit = {},
    showMenu: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        //horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Dot(isOnline = isConnecting)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = connectInfo, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.weight(1f))
        Column {
            IconButton(onClick = { onAction(SerialPortAction.ShowMenu) }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { onAction(SerialPortAction.ShowMenu) }) {
                DropdownMenuItem(
                    text = { Text(text = "Setting") },
                    onClick = { onAction(SerialPortAction.ShowSetting) },
                    leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text(text = "Clean log") },
                    onClick = { onAction(SerialPortAction.CleanLog) },
                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) }
                )
            }
        }
    }
}