package com.connor.episode.domain.model.preference

import com.connor.episode.domain.model.business.SelectType
import kotlinx.serialization.Serializable

@Serializable
data class NetPreferences(
    val serverPort: Int = 8080,
    val clientIP: String = "",
    val clientPort: Int = 8080,
    val lastSelectType: SelectType = SelectType.Server,
    val settings: BottomBarSettings = BottomBarSettings()
)