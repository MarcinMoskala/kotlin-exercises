package examples

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    flow {
        (1..5).forEach {
            delay(1000)
            emit(it)
            if (it == 2) throw RuntimeException("Error on $it")
        }
    }.onEach { println("On each $it") }
        .onStart { println("Starting flow") }
        .onCompletion { println("Flow completed") }
        .catch { ex -> println("Exception message: ${ex.message}") }
        .toList()
}
