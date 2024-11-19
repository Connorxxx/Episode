package com.connor.episode.errors

sealed class SerialPortError(open val msg: String) {
    data class Open(override val msg: String) : SerialPortError(msg)
    data class Write(override val msg: String) : SerialPortError(msg)
    sealed class Read(override val msg: String) : SerialPortError(msg) {
        data class EndOfStream(override val msg: String = "End of stream reached") : Read(msg)
        data class NoData(override val msg: String = "No data available") : Read(msg)
        data class IO(override val msg: String) : Read(msg)
    }
}