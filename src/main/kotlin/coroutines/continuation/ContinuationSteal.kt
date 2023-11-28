package continuation

import kotlinx.coroutines.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun main(): Unit = runBlocking<Unit> {
    launch {
        continuationSteal()
    }
    delay(1000)
    continuation?.resume("This is some text")
}

var continuation: Continuation<String>? = null

suspend fun continuationSteal(console: Console = Console()) {
    console.println("Before")
    // TODO: Suspend in here and store continuation in the `continuation` variable.
    // USE suspendCancellableCoroutine instead of suspendCoroutine
    // TODO: After continuation resume, print using `console` the value that was passed.
    console.println("After")
}

open class Console {

    open fun println(text: Any?) {
        kotlin.io.println(text)
    }
}
