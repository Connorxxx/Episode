package com.connor.episode.features.common.ui.common

import android.view.ViewConfiguration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Preview(showBackground = true)
@Composable
fun Dot(isOnline: Boolean = true) {
    Icon(
        imageVector = if (isOnline) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface
    )
}

@Preview(showBackground = true)
@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    number: Int = 0,
    onValueChange: (Int) -> Unit = {},
    range: IntRange = 1..100
) {

    var value by rememberSaveable { mutableIntStateOf(number) }

    var longClickDecrease by remember { mutableStateOf(false) }
    var longClickIncrease by remember { mutableStateOf(false) }

    LaunchedEffect(longClickDecrease) {
        while (longClickDecrease) {
            if (value > range.first) value--
            delay(100)
        }
    }
    LaunchedEffect(longClickIncrease) {
        while (longClickIncrease) {
            if (value < range.last) value++
            delay(100)
        }
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PressIconButton(
            onClick = {
                if (value > range.first) onValueChange(--value)
            },
            onLongClick = { longClickDecrease = true },
            onLongClickRelease = {
                longClickDecrease = false
                onValueChange(value)
            }
        ) {
            Icon(Icons.Default.ChevronLeft, "Decrease")
        }

        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 20.dp, vertical = 6.dp)
        )
        PressIconButton(
            onClick = { if (value < range.last) onValueChange(++value) },
            onLongClick = { longClickIncrease = true },
            onLongClickRelease = {
                longClickIncrease = false
                onValueChange(value)
            }
        ) {
            Icon(Icons.Default.ChevronRight, "Increase")
        }
    }
}

@Composable
fun PressIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onLongClickRelease: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    var isLongPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(ViewConfiguration.getLongPressTimeout().toLong())
            isLongPressed = true
            onLongClick()
        } else {
            if (isLongPressed) {
                onLongClickRelease()
                isLongPressed = false
            }
        }
    }
    Surface(
        modifier = modifier.minimumInteractiveComponentSize().size(40.dp),
        onClick = { if (!isLongPressed) onClick() },
        interactionSource = interactionSource,
        enabled = true,
        shape = CircleShape
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.alpha(if (isPressed) 0.5f else 1f)
        ) {
            content()
        }
    }
}

@Composable
fun StatusIndicator(
    modifier: Modifier = Modifier,
    isSuccess: Boolean,
) {

    val successColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error


    val backgroundColor by animateColorAsState(
        targetValue = if (isSuccess) successColor else errorColor,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxSize(),
        shape = CircleShape,
        color = backgroundColor,
        tonalElevation = 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Filled.Check else Icons.Filled.PriorityHigh,
                contentDescription = if (isSuccess) "成功" else "错误",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(9.dp)
            )
        }
    }
}

private fun Modifier.paddingLayout(fraction: Float) = this.layout { measurable, constraints ->
    val paddingPx = (minOf(constraints.maxWidth, constraints.maxHeight) * fraction).toInt()
    val placeable = measurable.measure(
        constraints.copy(
            maxWidth = constraints.maxWidth - paddingPx * 2,
            maxHeight = constraints.maxHeight - paddingPx * 2
        )
    )
    layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.place(paddingPx, paddingPx)
    }
}

// 预览函数
@Preview(showBackground = true, widthDp = 80, heightDp = 80)
@Composable
fun StatusIndicatorPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            StatusIndicator(
                isSuccess = true,
                modifier = Modifier.fillMaxSize(0.7f)
            )
        }
    }
}