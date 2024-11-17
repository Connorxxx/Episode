package com.connor.episode.errors

sealed class SerialPortError(open val msg: String) {
    data class Open(override val msg: String) : SerialPortError(msg)
    data class Write(override val msg: String) : SerialPortError(msg)
    data class Read(override val msg: String) : SerialPortError(msg)
    data class None(override val msg: String) : SerialPortError(msg)
}