package com.connor.episode.data.local.datastore.preference

import kotlinx.serialization.Serializable

@Serializable
data class SerialPortPreferences(
    val serialPort: String = "",
    val baudRate: String = "9600",
    val resend: Boolean = false,
    val resendSeconds: Int = 1,
    val sendFormatIdx: Int = 0,
    val receiveFormatIdx: Int = 0
)