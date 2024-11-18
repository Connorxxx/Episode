package com.connor.episode.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun Dot(isOnline: Boolean = true) {
    Icon(
        imageVector = if (isOnline) Icons.Rounded.CheckCircle else Icons.Filled.Cancel,
        contentDescription = null,
        tint = if (isOnline) MaterialTheme.colorScheme.primary else Color.Red
    )
}