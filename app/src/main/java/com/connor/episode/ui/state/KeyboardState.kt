package com.connor.episode.ui.state

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity

@Composable
fun rememberKeyboardState() = run {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    rememberUpdatedState(isImeVisible)
}