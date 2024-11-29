package com.connor.episode.domain.model.preference

import kotlinx.serialization.Serializable

@Serializable
data class SerialPortPreferences(
    val serialPort: String = "",
    val baudRate: String = "9600",
    val settings: BottomBarSettings = BottomBarSettings()
)