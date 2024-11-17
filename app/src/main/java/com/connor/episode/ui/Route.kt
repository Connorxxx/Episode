package com.connor.episode.ui

//Main //TODO
const val HOME = "home"

sealed class HomeRoute(val title: String, val route: String) {
    data object SerialPort : HomeRoute("SerialPort", "serial_port")
    data object Tcp : HomeRoute("Tcp", "tcp")
    data object Udp : HomeRoute("Udp", "udp")
    data object WebSocket : HomeRoute("WebSocket", "websocket")

    companion object {
        val routes = listOf(SerialPort, Tcp, Udp, WebSocket)
        val startDestination = SerialPort.route
    }
}