package coroutines.debug

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

suspend fun computeValue(): String = coroutineScope {
    val one = async { computeOne() }
    val two = async { computeTwo() }
    one.await() + two.await()
}

suspend fun computeOne(): String {
    delay(5000)
    return "4"
}

suspend fun computeTwo(): String {
    delay(5000)
    return "2"
}

// Add kotlinx-coroutines-debug
fun main() = runBlocking {
//    DebugProbes.install()
    val deferred = async { computeValue() }
    // Delay for some time
    delay(1000)
    println("Dumping all coroutines")
//    DebugProbes.dumpCoroutines()
    println("\nDumping only deferred")
//    DebugProbes.printJob(deferred)
}






















