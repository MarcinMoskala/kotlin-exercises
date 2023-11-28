import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.selects.*

fun CoroutineScope.produceNumbers(side: SendChannel<Int>) = produce<Int> {
    for (num in 1..100) {
        delay(100)
        send(num)
//        select<Unit> {
//            onSend(num) {}
//            side.onSend(num) {}
//        }
    }
}

fun main() = runBlocking<Unit> {
    val side = Channel<Int>()
    launch {
        side.consumeEach { println("Side channel has $it") }
    }
    produceNumbers(side).consumeEach {
        println("Consuming $it")
        delay(250)
    }
    println("Done consuming")
    coroutineContext.cancelChildren()
}