package com.connor.episode.domain.model.error

sealed interface SerialPortError : Error {
    data class Open(override val msg: String) : SerialPortError
    data class Write(override val msg: String) : SerialPortError
    sealed interface Read : SerialPortError {
        data class EndOfStream(override val msg: String = "End of stream reached") : Read
        data class NoData(override val msg: String = "No data available") : Read
        data class IO(override val msg: String) : Read
    }
}

