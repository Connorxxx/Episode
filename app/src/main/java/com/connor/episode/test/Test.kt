package com.connor.episode.test

import arrow.core.Either
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

fun main(): Unit = runBlocking {
    test().collect {
        println(it)
    }
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