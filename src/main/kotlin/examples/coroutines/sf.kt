package examples.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.milliseconds

//suspend fun main(): Unit = coroutineScope {
//    val sf = MutableSharedFlow<String>(replay = 0)
//    // or MutableSharedFlow<String>()
//
//    sf.onEach { println("#1 received $it") }
//        .launchIn(this)
//    sf.onEach { println("#2 received $it") }
//        .launchIn(this)
//
//    delay(1000)
//    sf.emit("Message1")
//    sf.emit("Message2")
//
//    delay(1000)
//    coroutineContext.job.cancelChildren()
//}

private val messagesFlow = flow {
    println("Establishing connection...")
    while (true) {
        println("Receiving message...")
        emit("Message from time ${LocalDateTime.now()}")
        delay(1000)
    }
}.onCompletion {
    println("Connection closed")
}

private fun CoroutineScope.startReceiverFor(
    num: Int,
    time: Long,
    flow: Flow<String>
) = launch {
    withTimeout(time) {
        flow.collect {
            println("#$num received $it")
        }
    }
}

//suspend fun main(): Unit = coroutineScope {
//    val flow: Flow<String> = messagesFlow.shareIn(
//        scope = this,
//        started = SharingStarted.Eagerly,
//        // replay = 0 (default)
//    )
//
//    delay(4000)
//    startReceiverFor(1, 2100, flow)
//    delay(3000)
//    startReceiverFor(2, 3100, flow)
//    startReceiverFor(3, 2100, flow)
//}

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
//    val state = MutableStateFlow(1) // must have value, reply = 1, conflated, ignores values equals to the previous one
//
//    // view
//    state.onEach { updateView(it) }
//        .launchIn(this)
//
//    // view model
//    launch { 
//        repeat(10000) {
//            state.update { it + 1 }
//            delay(1)
//        }
//    }
//}
//
//suspend fun updateView(i: Int) {
//    delay(100)
//    println("Observed value $i")
//}

//suspend fun main(): Unit = coroutineScope {
//    val sf: StateFlow<String> = messagesFlow.stateIn(
//        scope = this,
//        started = SharingStarted.Eagerly,
//        initialValue = ""
//    )
//
//    delay(2000)
//    startReceiverFor(1, 2100, sf)
//    delay(3000)
//    startReceiverFor(2, 3100, sf)
//    startReceiverFor(3, 2100, sf)
//}
