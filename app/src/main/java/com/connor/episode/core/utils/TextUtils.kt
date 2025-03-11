package com.connor.episode.core.utils

fun filterHex(msg: String, input: String): String {
    val hex = input.uppercase()
    return if (input.length > msg.length) hex.filter { it.isDigit() || it in 'A'..'F' }.chunked(2)
        .joinToString(" ")
    else hex.filter { it.isDigit() || it in 'A'..'F' || it == ' ' }
}

fun String.validateIp(): Boolean {
    return trim()
        .takeUnless { it.isEmpty() }?.let { ip ->
            ip.split(".").takeIf { segments ->
                segments.size == 4 && segments.none { it.isEmpty() }
            }?.map { segment ->
                segment.toIntOrNull()?.takeIf {
                    it in 0..255 && (segment.length == 1 || !segment.startsWith("0"))
                }
            }?.all { it != null }
        } == true
}

fun String.validatePort(): Boolean {
    return trim()
        .takeUnless { it.isEmpty() }
        ?.let { port ->
            port.toIntOrNull()?.takeIf {
                    it in 0..65535 && (port.length == 1 || !port.startsWith("0"))
                }
        } != null
}