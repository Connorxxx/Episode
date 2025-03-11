package com.connor.episode.features.common.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.connor.episode.domain.model.uimodel.TopBarAction
import com.connor.episode.features.common.ui.theme.EpisodeTheme

@Composable
fun TopBar(
    isConnecting: Boolean = false,
    connectInfo: String = "ttyS0 : 9600",
    onAction: (TopBarAction) -> Unit = {},
) {
    var showMenu by remember { mutableStateOf(false) }
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
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(text = "Setting") },
                    onClick = {
                        showMenu = false
                        onAction(TopBarAction.IsShowSettingDialog(true))
                    },
                    leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text(text = "Clean log") },
                    onClick = {
                        showMenu = false
                        onAction(TopBarAction.CleanLog)
                    },
                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) }
                )
                if (isConnecting) DropdownMenuItem(
                    text = { Text(text = "Close") },
                    onClick = {
                        showMenu = false
                        onAction(TopBarAction.Close)
                    },
                    leadingIcon = { Icon(Icons.Outlined.Close, contentDescription = null) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    EpisodeTheme {
        TopBar()
    }
}