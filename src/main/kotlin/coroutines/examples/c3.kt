package examples

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    val channel = Channel<String>()
    // Same as Channel<String>(Channel.RENDEZVOUS)

    launch {
        repeat(5) {
            channel.send("Ping $it")
            println("Message sent")
        }
        channel.close()
    }

    // Listener
    launch {
        delay(1000)
        for (text in channel) {
            println(text)
            delay(1000)
        }
    }
}
