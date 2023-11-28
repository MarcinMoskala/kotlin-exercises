package examples.c1

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val channel = Channel<Int>()
    launch {
        repeat(5) { index ->
            println("Producing next one")
            channel.send(index * 2)
            delay(1000)
        }
    }

    repeat(5) {
        val received = channel.receive()
        println(received)
    }
}
