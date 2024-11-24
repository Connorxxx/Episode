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
    sendFormatIdx = sendFormat,
    receiveFormatIdx = receiveFormat,
    extraInfo = if (serialPorts.isEmpty()) "No Serial Ports Found" else "Closed"
)

fun SerialPortState.toPreferences() = SerialPortPreferences(
    serialPort = serialPort,
    baudRate = baudRate,
    resend = resend,
    resendSeconds = resendSeconds,
    sendFormatIdx = sendFormatIdx,
    receiveFormatIdx = receiveFormatIdx
)

fun SerialPortState.updateFromPref(preferences: SerialPortPreferences) = copy(
    serialPort = preferences.serialPort,
    baudRate = preferences.baudRate,
    resend = preferences.resend,
    resendSeconds = preferences.resendSeconds,
    sendFormatIdx = preferences.sendFormatIdx,
    receiveFormatIdx = preferences.receiveFormatIdx
)