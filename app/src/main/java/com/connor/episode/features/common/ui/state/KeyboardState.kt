package com.connor.episode.features.common.ui.state

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun rememberKeyboardState() = run {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    rememberUpdatedState(isImeVisible)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun rememberKeyboardAnimationCompleteState(
    debounceTime: Long = 100L
) = run {
    val density = LocalDensity.current
    val imeHeight = WindowInsets.ime.getBottom(density)
    val imeVisible = WindowInsets.isImeVisible

    val keyboardVisibleState= remember { mutableStateOf(false) }
    val lastHeight = remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val debounceJob = remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(imeHeight, imeVisible) {
        debounceJob.value?.cancel()

        debounceJob.value = coroutineScope.launch {
            // 如果键盘不可见，为false
            if (!imeVisible) {
                keyboardVisibleState.value = false
                lastHeight.intValue = 0
                return@launch
            }
            // 如果键盘可见但高度发生变化，等待动画完成
            delay(debounceTime)

            // 只有当高度稳定且大于0时才视为弹起完成
            if (imeHeight > 0 && lastHeight.intValue != imeHeight) {
                lastHeight.intValue = imeHeight
                keyboardVisibleState.value = true
            }
        }
    }
    keyboardVisibleState
}