package coroutines.debug

import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.days

fun main() {
//    System.setProperty("kotlinx.coroutines.debug", "on")
    GlobalScope.launch { a() }
    Thread.sleep(1000)
    1.days
}

suspend fun a() = coroutineScope {
    val b = async { b() }
    val c = async { c() }
    b.await() + c.await()
}

suspend fun b(): Int {
    delay(1000)
    return 123
}

suspend fun c(): Int {
    delay(500)
    error("Error in c")
}