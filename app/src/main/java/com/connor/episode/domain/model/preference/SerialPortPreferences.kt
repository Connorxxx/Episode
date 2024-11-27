package com.connor.episode.domain.model.preference

import com.connor.episode.domain.model.business.SerialPortSettings
import kotlinx.serialization.Serializable

@Serializable
data class SerialPortPreferences(
    val serialPort: String = "",
    val baudRate: String = "9600",
    val settings: SerialPortSettings  = SerialPortSettings()
)