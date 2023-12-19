package coffee.dispatchers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

val dispatcher = Dispatchers.IO.limitedParallelism(1)
//val dispatcher = Dispatchers.Default
//val dispatcher = Dispatchers.IO
//val dispatcher = Dispatchers.IO.limitedParallelism(100)

//val operation = ::cpu1
//val operation = ::blocking
val operation = ::suspending

fun cpu1() {
    var i = Int.MAX_VALUE
    while (i > 0) i -= if (i % 2 == 0) 1 else 2
}

fun blocking() {
    Thread.sleep(1000)
}

suspend fun suspending() {
    delay(1000)
}

suspend fun main() = measureTimeMillis {
    coroutineScope {    
        repeat(100) {
            launch(dispatcher) {
                operation()
                println("Done $it")
            }
        }
    }
}.let { println("Took $it") }
