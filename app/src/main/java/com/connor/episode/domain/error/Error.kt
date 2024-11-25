package com.connor.episode.domain.error

data class Error(
    val msg: String = "",
    val isFatal: Boolean = false
)