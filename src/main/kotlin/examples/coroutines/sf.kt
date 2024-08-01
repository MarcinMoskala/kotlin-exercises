package examples.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds

//suspend fun main(): Unit = coroutineScope {
//    val mutableSharedFlow = MutableSharedFlow<String>(replay = 0)
//    // or MutableSharedFlow<String>()
//
//    launch {
//        mutableSharedFlow.collect {
//            println("#1 received $it")
//        }
//    }
//    launch {
//        mutableSharedFlow.collect {
//            println("#2 received $it")
//        }
//    }
//
//    delay(1000)
//    mutableSharedFlow.emit("Message1")
//    mutableSharedFlow.emit("Message2")
//}

private val messagesFlow = flow {
    println("Establishing connection...")
    while (true) {
        println("Receiving message...")
        emit("Message")
        delay(1000)
    }
}.onCompletion {
    println("Connection closed")
}

private fun CoroutineScope.startReceiverFor(
    num: Int, 
    time: Long,
    sharedFlow: SharedFlow<String>
) = launch {
    withTimeout(time) {
        sharedFlow.collect {
            println("#$num received $it")
        }
    }
}

//suspend fun main(): Unit = coroutineScope {
//    val sharedFlow: SharedFlow<String> = messagesFlow.shareIn(
//        scope = this,
//        started = SharingStarted.Eagerly,
//        // replay = 0 (default)
//    )
//
//    delay(2000)
//    startReceiverFor(1, 2100, sharedFlow)
//    delay(3000)
//    startReceiverFor(2, 3100, sharedFlow)
//    startReceiverFor(3, 2100, sharedFlow)
//}
//

//suspend fun main() = coroutineScope {
//   val state = MutableStateFlow(1)
//   println(state.value) // 1
//   delay(1000)
//   launch {
//       state.collect { println("Value changed to $it") } // Value changed to 1
//   }
//   delay(1000)
//   state.value = 2 // Value changed to 2
//   delay(1000)
//   launch {
//       state.collect { println("and now it is $it") } // and now it is 2
//   }
//   delay(1000)
//   state.value = 3 // Value changed to 3 and now it is 3
//}

//suspend fun main(): Unit = coroutineScope {
//    val sharedFlow: SharedFlow<String> = messagesFlow.shareIn(
//        scope = this,
//        started = SharingStarted.WhileSubscribed(stopTimeout = 1000.milliseconds),
//        // replay = 0 (default)
//    )
//
//    delay(2000)
//    startReceiverFor(1, 2100, sharedFlow)
//    delay(3000)
//    startReceiverFor(2, 3100, sharedFlow)
//    startReceiverFor(3, 2100, sharedFlow)
//}
