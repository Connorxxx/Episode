package com.connor.episode.core.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

fun <T> Flow<T>.withTimeout(timeout: Duration): Flow<T> = flow {
    withTimeout(timeout.inWholeMilliseconds) {
        collect { value ->
            emit(value)
        }
    }
}