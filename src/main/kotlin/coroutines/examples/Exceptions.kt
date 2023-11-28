package examples

import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    try {
        launch {
            throwing()
        }
    } catch (e: IllegalStateException) {
        print("Caught")
    }
    print("Done")
}

//fun main() = runBlocking {
//    try {
//        val async = async {
//            throwing()
//        }
//        async.await()
//    } catch (e: IllegalStateException) {
//        print("Caught")
//    }
//    print("Done")
//}
//
//fun main() = runBlocking<Unit> {
//    try {
//        coroutineScope {
//            throwing()
//        }
//    } catch (e: IllegalStateException) {
//        print("Caught")
//    }
//}
//
//fun main() = runBlocking {
//    val channel = produce(capacity = UNLIMITED) {
//        send(1)
//        send(2)
//        throwing()
//    }
//    delay(100)
//    try {
//        for (e in channel) {
//            println("Got it")
//        }
//    } catch (e: IllegalStateException) {
//        println("Caught")
//    }
//}
//
//fun main() = runBlocking<Unit> {
//    val flow = flow {
//        emit(1)
//        emit(2)
//        throwing()
//    }
//    try {
//        flow.collect { println("Got it") }
//    } catch (e: IllegalStateException) {
//        println("Caught")
//    }
//}

fun throwing() {
    throw IllegalStateException()
}
