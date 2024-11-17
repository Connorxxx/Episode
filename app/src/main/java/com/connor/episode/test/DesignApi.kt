package com.connor.episode.test

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

//方式1
class HttpClient(private val userConfig: ClientConfig = ClientConfig()) {
    fun get(path: String) {
        println("Requesting ${userConfig.baseUrl}/$path with timeout ${userConfig.timeout}")
    }
}

class ClientConfig {
    var baseUrl: String = "http://localhost:8080"
    var timeout: Duration = 30.seconds
}

fun HttpClient(block: ClientConfig.() -> Unit): HttpClient =
    HttpClient(ClientConfig().apply(block))

//方式2
class HttpClient2(
    private val url: String= "http://localhost:8080",
    private val timeout: Duration = 30.seconds
) {
    fun get(path: String) {
        println("Requesting $url/$path with timeout $timeout")
    }
}

fun use() {
    val client = HttpClient {
        baseUrl = "http://localhost:8080"
        timeout = 30.seconds
    }
    client.get("user")
}