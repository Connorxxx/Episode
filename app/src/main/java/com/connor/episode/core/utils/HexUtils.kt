package com.connor.episode.core.utils

fun String.hexStringToByteArray(): ByteArray {

    val hex = replace("\\s+".toRegex(), "").uppercase()

    if (hex.isEmpty()) return ByteArray(0)

    if (!hex.all { it.isDigit() || it in 'A'..'F' }) error("Invalid hex string character $hex")

    val normalized = if (hex.length % 2 != 0) "0$hex" else hex

    return normalized
        .chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

fun String.hexStringToAscii(): String {
    val hex = replace("\\s+".toRegex(), "").uppercase()

    if (!hex.all { it.isDigit() || it in 'A'..'F' }) error("Invalid hex string character $hex")

    return hex
        .chunked(2)
        .map { it.toInt(16).toChar() }
        .joinToString("")
}

fun String.asciiToHexString() =
    map { it.code.toString(16).uppercase().padStart(2, '0') }.joinToString(" ")
