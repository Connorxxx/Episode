package com.connor.episode.domain.model.error

data class UiError(
    val msg: String = "",
    val isFatal: Boolean = false
)