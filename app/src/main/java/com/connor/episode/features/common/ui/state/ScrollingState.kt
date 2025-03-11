package com.connor.episode.features.common.ui.state

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun LazyListState.rememberIsScrollingUp(): State<Boolean> {
    var previousScrollPosition by remember {
        mutableStateOf(ScrollPosition(firstVisibleItemIndex, firstVisibleItemScrollOffset))
    }

    return remember {
        derivedStateOf {
            val currentPosition = ScrollPosition(firstVisibleItemIndex, firstVisibleItemScrollOffset)
            val isScrollingUp = when {
                currentPosition.index != previousScrollPosition.index ->
                    currentPosition.index < previousScrollPosition.index
                currentPosition.offset != previousScrollPosition.offset ->
                    currentPosition.offset < previousScrollPosition.offset
                else -> true
            }

            previousScrollPosition = currentPosition
            isScrollingUp
        }
    }
}

@Composable
fun LazyListState.rememberIsScrolling(): State<Boolean> {
    return remember {
        derivedStateOf { isScrollInProgress }
    }
}

private data class ScrollPosition(
    val index: Int,
    val offset: Int
)