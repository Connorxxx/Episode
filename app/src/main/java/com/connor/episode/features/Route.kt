package com.connor.episode.features

//Main //TODO
const val HOME = "home"

sealed class HomeRoute(val title: String, val route: String) {
    data object SerialPort : HomeRoute("SerialPort", "serial_port")
    data object Tcp : HomeRoute("TCP", "tcp")
    data object Udp : HomeRoute("UDP", "udp")
    data object WebSocket : HomeRoute("WebSocket", "websocket")
    data object Ble : HomeRoute("BLE", "ble")

    companion object {
        val routes = listOf(SerialPort, Tcp, Udp, WebSocket, Ble)
        val startDestination = SerialPort.route
    }
}