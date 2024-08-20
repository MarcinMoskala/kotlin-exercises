package coroutines.examples.sus

import kotlin.coroutines.*
import kotlinx.coroutines.*

suspend fun main() {
    println("Before")

    

    println("After")
}

//private val executor = Executors.newSingleThreadScheduledExecutor {
//    Thread(it, "scheduler").apply { isDaemon = true }
//}

//executor.schedule({}, 1000, TimeUnit.MILLISECONDS)

//val i: Int = suspendCancellableCoroutine<Int> { cont ->
//    cont.resume(42)
//}
//println(i) // 42
//
//val str: String = suspendCancellableCoroutine<String> { cont ->
//    cont.resume("Some text")
//}
//println(str) // Some text
//
//val b: Boolean = suspendCancellableCoroutine<Boolean> { cont ->
//    cont.resume(true)
//}
//println(b) // true

//fun fetchUser(callback: (User) -> Unit) {
//    thread {
//        Thread.sleep(1000)
//        callback(User("Test"))
//    }
//}
//fun fetchUser(callback: (User) -> Unit): Call {
//    thread {
//        Thread.sleep(1000)
//        callback(User("Test"))
//    }
//    return Call()
//}
//fun fetchUser(onSuccess: (User) -> Unit, onError: (Throwable) -> Unit): Call {
//    thread {
//        Thread.sleep(1000)
//        if (Random.nextBoolean()) {
//            onSuccess(User("Test"))
//        } else {
//            onError(ApiException())
//        }
//    }
//    return Call()
//}

class User(val name: String)
class ApiException: Throwable()
class Call {
    fun cancel() {}
}
