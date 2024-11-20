package com.connor.episode.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Dot(isOnline: Boolean = true) {
    Icon(
        imageVector = if (isOnline) Icons.Rounded.CheckCircle else Icons.Filled.Cancel,
        contentDescription = null,
        tint = if (isOnline) MaterialTheme.colorScheme.primary else Color.Red
    )
}

@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange = 0..100
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier.size(30.dp),
            onClick = {
                if (value > range.first) onValueChange(value - 1)
            }
        ) {
            Icon(Icons.Default.ChevronLeft, "Increase")
        }

        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        IconButton(
            modifier = Modifier.size(30.dp),
            onClick = {
                if (value < range.last) onValueChange(value + 1)
            }
        ) {
            Icon(Icons.Default.ChevronRight, "Decrease")
        }
    }
}