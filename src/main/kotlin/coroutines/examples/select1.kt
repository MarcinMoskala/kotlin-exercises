package examples

import kotlinx.coroutines.channels.*
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select

suspend fun CoroutineScope.produceString(s: String, time: Long) = produce {
    while (true) {
        delay(time)
        send(s)
    }
}

fun main() = runBlocking {
    val fooChannel = produceString("foo", 200L)
    val barChannel = produceString("BAR", 500L)

    repeat(50) {
        select {
            fooChannel.onReceive { println("From fooChannel: $it") }
            barChannel.onReceive { println("From barChannel: $it") }
        }
    }

    coroutineContext.cancelChildren()
}
