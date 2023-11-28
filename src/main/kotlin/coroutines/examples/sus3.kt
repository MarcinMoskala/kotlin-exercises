package examples

import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun main() {
    println("Before")

    suspendCoroutine<Unit> { continuation ->
        thread {
            Thread.sleep(1000)
            continuation.resume(Unit)
        }
    }

    println("After")
}