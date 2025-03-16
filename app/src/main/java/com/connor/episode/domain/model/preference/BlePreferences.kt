package com.connor.episode.domain.model.preference

import com.connor.episode.domain.model.business.SelectType
import kotlinx.serialization.Serializable

@Serializable
data class BlePreferences(
    val lastSelectType: SelectType = SelectType.Server,
    val settings: BottomBarSettings = BottomBarSettings()
)
