package com.connor.episode.domain.model.error

sealed interface BleError : EpisodeError {
    data class Start(override val msg: String) : BleError
    data class Connect(override val msg: String) : BleError
    data class Read(override val msg: String) : BleError
    data class Write(override val msg: String) : BleError
    data class Scan(override val msg: String) : BleError
    data class ScanTimeout(override val msg: String) : BleError
    data class Permission(override val msg: String) : BleError
    data class Advertise(override val msg: String) : BleError
    data class AdvertiseTimeout(override val msg: String) : BleError
}