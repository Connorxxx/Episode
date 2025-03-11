package com.connor.episode.core.utils

fun getBytesMsg(type: Int, msg: String) = if (type == 0) msg.hexStringToByteArray()
else msg.toByteArray(Charsets.US_ASCII)