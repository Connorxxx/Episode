package com.connor.episode.domain.model.business

data class NetModel(
    val server: ServerModel = ServerModel(),
    val client: ClientModel = ClientModel(),
)

data class ServerModel(
    val localIp: String = "",
    val port: Int = 8080,
    val acceptClients: List<String> = emptyList(), //TODO: not use yet
)

data class ClientModel(
    val ip: String = "",
    val port: Int = 8080,
)

enum class SelectType {
    Server,Client
}

enum class NetResult {
    Server,Client,Close,Error
}