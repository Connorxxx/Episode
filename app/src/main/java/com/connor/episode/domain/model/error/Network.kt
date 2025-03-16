package com.connor.episode.domain.model.error

sealed interface NetworkError : EpisodeError {
    data class Connect(override val msg: String) : NetworkError
    data class Accept(override val msg: String) : NetworkError
    data class Read(override val msg: String, val ip: String) : NetworkError
    data class Write(override val msg: String, val ip: String) : NetworkError
}
