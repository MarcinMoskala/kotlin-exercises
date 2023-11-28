package examples

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

//suspend fun getConfig(): String {
//    println("A")
//    delay(1000)
//    println("B")
//    return "Some config"
//}

fun getConfig(continuation: Continuation<String>): Any  /* String & COROUTINE_SUSPENDED */ {
    val cont = continuation as? `CoroutineExampleKt$getConfig` ?: `CoroutineExampleKt$getConfig`(continuation)

    if (cont.label == 0) {
        println("A")
        cont.label = 1
        if (_delay(cont, 1000) == COROUTINE_SUSPENDED) {
            return COROUTINE_SUSPENDED
        }
    }
    if (cont.label == 1) {
        println("B")
        return "Some config"
    }
    error("Impossible")
}

class `CoroutineExampleKt$getConfig`(val continuation: Continuation<*>) : Continuation<String> {
    var label = 0

    override val context: CoroutineContext
        get() = continuation.context

    override fun resumeWith(result: Result<String>) {
        // ???
    }
}

// Coroutine internals
val COROUTINE_SUSPENDED = Any()

val EXECUTOR = Executors.newSingleThreadScheduledExecutor {
    Thread(it, "scheduler").apply { isDaemon = true }
}

fun _delay(continuation: Continuation<*>, time: Long): Any? {
    val cont = continuation as? `CoroutineExampleKt$delay` ?: `CoroutineExampleKt$delay`(continuation)
    EXECUTOR.schedule({ cont.resume(Unit) }, time, SECONDS)
    return COROUTINE_SUSPENDED
}

class `CoroutineExampleKt$delay`(val continuation: Continuation<*>) : Continuation<Unit> {
    var label = 0

    override val context: CoroutineContext
        get() = continuation.context

    override fun resumeWith(result: Result<Unit>) {
//        continuation.resume()
    }
}