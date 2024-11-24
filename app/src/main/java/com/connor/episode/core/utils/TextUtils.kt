package com.connor.episode.core.utils

fun filterHex(msg: String, input: String): String {
    val hex = input.uppercase()
    return if (input.length > msg.length) hex.filter { it.isDigit() || it in 'A'..'F' }.chunked(2).joinToString(" ")
    else hex.filter { it.isDigit() || it in 'A'..'F' || it == ' ' }
}