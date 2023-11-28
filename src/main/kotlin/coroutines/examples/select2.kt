package examples.select2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.onReceiveOrNull
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select

suspend private fun CoroutineScope.produceString(s: String, time: Long) = produce {
    repeat(20) {
        delay(time)
        send(s)
    }
}

@Suppress("DEPRECATION")
fun main() = runBlocking {
    val fooChannel = produceString("foo", 200L)
    val barChannel = produceString("BAR", 500L)

    repeat(40) {
        delay(200)
        select { // selectUnbiased
            fooChannel.onReceive { println("From fooChannel: $it") }
            barChannel.onReceive { println("From barChannel: $it") }
        }
    }
}
