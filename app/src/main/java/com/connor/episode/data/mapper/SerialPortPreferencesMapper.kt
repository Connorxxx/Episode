package com.connor.episode.data.mapper

import com.connor.episode.data.local.datastore.preference.SerialPortPreferences
import com.connor.episode.domain.model.SerialPortModel

fun SerialPortModel.toPreferences() = SerialPortPreferences(
    serialPort = serialPort,
    baudRate = baudRate,
    resend = resend,
    resendSeconds = resendSeconds,
    sendFormat = sendFormat,
    receiveFormat = receiveFormat
)

fun SerialPortPreferences.toModel() = SerialPortModel(
    serialPort = serialPort,
    baudRate = baudRate,
    resend = resend,
    resendSeconds = resendSeconds,
    sendFormat = sendFormat,
    receiveFormat = receiveFormat
)