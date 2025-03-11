package com.connor.episode.domain.model.business


data class SerialPortModel(
    val serialPorts: List<SerialPortDevice> = emptyList(),
    val portName: String = "",
    val baudRate: String = "",
)

data class SerialPortDevice(
    val name: String = "",
    val path: String = ""
)