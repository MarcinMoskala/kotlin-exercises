import kotlinx.coroutines.*
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.selects.select

suspend fun requestData1(): String {
    delay(100_000)
    return "Data1"
}

suspend fun requestData2(): String {
    delay(1000)
    return "Data2"
}

suspend fun askMultipleForData(): String = coroutineScope {
    select<String> {
        async { requestData1() }.onAwait { it }
        async { requestData2() }.onAwait { it }
    }.also { coroutineContext.cancelChildren() }
}

//suspend fun main(): Unit = coroutineScope {
//    println(askMultipleForData())
//}

suspend fun CoroutineScope.produceString(s: String, time: Long) = produce {
    repeat(4) {
        delay(time)
        send(s)
    }
}

//fun main() = runBlocking {
//    val fooChannel = produceString("foo", 210L)
//    val barChannel = produceString("BAR", 500L)
//
//    repeat(7) {
//        select {
//            fooChannel.onReceive {
//                println("From fooChannel: $it")
//            }
//            barChannel.onReceive {
//                println("From barChannel: $it")
//            }
//        }
//    }
//
//    coroutineContext.cancelChildren()
//}

//fun main() = runBlocking {
//    val fooChannel = produceString("foo", 210L)
//    val barChannel = produceString("BAR", 500L)
//
//    repeat(7) {
//        select {
//            if (!fooChannel.isClosedForReceive) {
//                fooChannel.onReceiveCatching {
//                    println("From fooChannel: $it")
//                }
//            }
//            if (!barChannel.isClosedForReceive) {
//                barChannel.onReceiveCatching {
//                    println("From barChannel: $it")
//                }
//            }
//        }
//    }
//
//    coroutineContext.cancelChildren()
//}

//fun main(): Unit = runBlocking {
//    val c1 = Channel<Char>(capacity = 2)
//    val c2 = Channel<Char>(capacity = 2)
//    
//    // Send values
//    launch {
//        for (c in 'A'..'H') {
//            delay(400)
//            select<Unit> {
//                c1.onSend(c) { println("Sent $c to 1") }
//                c2.onSend(c) { println("Sent $c to 2") }
//            }
//        }
//    }
//    
//    // Receive values
//    launch {
//        while (true) {
//            delay(1000)
//            val c = select<String> {
//                c1.onReceive { "$it from 1" }
//                c2.onReceive { "$it from 2" }
//            }
//            println("Received $c")
//        }
//    }
//}
