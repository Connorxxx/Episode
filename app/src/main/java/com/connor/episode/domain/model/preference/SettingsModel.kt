package com.connor.episode.domain.model.preference

import kotlinx.serialization.Serializable

@Serializable
data class BottomBarSettings(
    val resend: Boolean = false,
    val resendSeconds: Int = 1,
    val sendFormat: Int = 0,
    val receiveFormat: Int = 0,
)