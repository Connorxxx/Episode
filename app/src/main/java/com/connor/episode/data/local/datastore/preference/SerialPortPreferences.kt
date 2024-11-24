package com.connor.episode.data.local.datastore.preference

import kotlinx.serialization.Serializable

@Serializable
data class SerialPortPreferences(
    val serialPort: String = "",
    val baudRate: String = "9600",
    val resend: Boolean = false,
    val resendSeconds: Int = 1,
    val sendFormat: Int = 0,
    val receiveFormat: Int = 0
)