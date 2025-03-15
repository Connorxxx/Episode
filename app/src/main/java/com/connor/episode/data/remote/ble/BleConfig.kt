package com.connor.episode.data.remote.ble

import java.util.UUID

object BleConfig {
    val SERVICE_UUID: UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb")
    val MESSAGE_CHARACTERISTIC_UUID: UUID = UUID.fromString("7db3e235-3608-41f3-a03c-955fcbd2ea4b")
    val CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    const val MAX_MESSAGE_LENGTH = 512 // BLE一次传输的最大字节
}