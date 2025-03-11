package com.connor.episode.test

import android.view.View
import arrow.core.Either
import com.connor.episode.core.utils.asciiToHexString
import com.connor.episode.core.utils.hexStringToAscii
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalStdlibApi::class)
fun main(): Unit = runBlocking {
    val bytes = "Hello world".toByteArray(Charsets.US_ASCII)
    val myway = "H w".asciiToHexString()
    val hexToAscii = "48 20 77 DD DD FF FC".hexStringToAscii()
    println(bytes.toHexString())
    println(myway)
    println(hexToAscii)
}

fun test() = flow {
    var i = 0
    while (true) {
        delay(1.seconds)
        i++
        val e = Either.catch { throwRw(i) }.mapLeft { "error" }
        emit(e)
        if (e.isLeft()) break
    }
}.flowOn(Dispatchers.IO)

fun throwRw(i: Int): Int {
    if (i >= 5) throw IOException("test")
    return i
}

fun View.clicks() = callbackFlow {
    setOnClickListener { trySend(Unit) }
    awaitClose { setOnClickListener(null) }
}

fun myTest(block: (String) -> Boolean) {
    val name = "test"
    val b = block(name)
    println(b)
}