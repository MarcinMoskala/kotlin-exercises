package examples.c9

import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    println("Started producing")
    val channel = produce<Int> {
        println("Channel started")
        for (i in 1..3) {
            delay(100)
            send(i)
        }
    }

    delay(150)
    println("Calling channel...")
    channel.consumeEach { value -> println(value) }
    println("Consuming again...")
    channel.consumeEach { value -> println(value) }
}

