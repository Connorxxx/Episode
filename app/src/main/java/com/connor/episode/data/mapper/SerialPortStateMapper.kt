package com.connor.episode.data.mapper

import com.connor.episode.data.local.datastore.preference.SerialPortPreferences
import com.connor.episode.domain.model.SerialPortModel
import com.connor.episode.features.serial.SerialPortState

fun SerialPortModel.toUiState() = SerialPortState(
    serialPorts = serialPorts,
    serialPort = serialPort,
    baudRate = baudRate,
    messages = messages,
    resend = resend,
    resendSeconds = resendSeconds,
    sendFormat = sendFormat,
    receiveFormat = receiveFormat,
    extraInfo = if (serialPorts.isEmpty()) "No Serial Ports Found" else "Closed"
)

fun SerialPortState.toModel() = SerialPortModel(
    serialPorts = serialPorts,
    serialPort = serialPort,
    baudRate = baudRate,
    messages = messages,
    resend = resend,
    resendSeconds = resendSeconds,
    sendFormat = sendFormat,
    receiveFormat = receiveFormat
)

fun SerialPortState.updateFromPref(preferences: SerialPortPreferences) = copy(
    serialPort = preferences.serialPort,
    baudRate = preferences.baudRate,
    resend = preferences.resend,
    resendSeconds = preferences.resendSeconds,
    sendFormat = preferences.sendFormat,
    receiveFormat = preferences.receiveFormat
)