package examples

import kotlinx.coroutines.*
import kotlin.coroutines.*

suspend fun main(): Unit = withContext(newSingleThreadContext("Name1")) {
    var continuation: Continuation<Unit>? = null

    launch(newSingleThreadContext("Name2")) {
        delay(1000)
        continuation?.resume(Unit)
    }

    launch(Dispatchers.Unconfined) {
        println(Thread.currentThread().name) // Name1

        suspendCoroutine<Unit> { continuation = it }

        println(Thread.currentThread().name) // Name2

        delay(1000)

        println(Thread.currentThread().name) // kotlinx.coroutines.DefaultExecutor
    }
}