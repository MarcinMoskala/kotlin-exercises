package examples

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    val channel = Channel<String>(Channel.CONFLATED)

    launch {
        var i = 1
        repeat(5) {
            channel.send("Ping ${i++}")
            println("Message sent")
        }
    }

    // Listener
    launch {
        var i = 1
        for (text in channel) {
            println(text)
            delay(1000)
        }
    }
}