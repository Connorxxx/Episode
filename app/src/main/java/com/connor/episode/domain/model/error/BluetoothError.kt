package com.connor.episode.domain.model.error

sealed interface BluetoothError : Error {
    data class Start(override val msg: String) : BluetoothError
    data class Connect(override val msg: String) : BluetoothError
    data class Read(override val msg: String) : BluetoothError
    data class Write(override val msg: String) : BluetoothError
    data class Scan(override val msg: String) : BluetoothError
    data class Permission(override val msg: String) : BluetoothError
    data class Advertise(override val msg: String) : BluetoothError
}