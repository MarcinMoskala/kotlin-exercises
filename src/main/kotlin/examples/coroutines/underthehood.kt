package examples.coroutines.underthehood

import kotlinx.coroutines.delay
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume

//suspend fun getUser(): User? = TODO()
//
//data class User(val name: String)

//suspend fun myFunction() {
//    println("Before")
//    delay(1000) // suspending
//    println("After")
//}

//fun myFunction(continuation: Continuation<Unit>): Any {
//    val continuation = continuation as? MyFunctionContinuation
//        ?: MyFunctionContinuation(continuation)
//
//    if (continuation.label == 0) {
//        println("Before")
//        continuation.label = 1
//        if (delay(1000, continuation) == COROUTINE_SUSPENDED) {
//            return COROUTINE_SUSPENDED
//        }
//    }
//    if (continuation.label == 1) {
//        println("After")
//        return Unit
//    }
//    error("Impossible")
//}
//
//class MyFunctionContinuation(
//    val completion: Continuation<Unit>
//) : Continuation<Unit> {
//    override val context: CoroutineContext
//        get() = completion.context
//
//    var label = 0
//    var result: Result<Any>? = null
//
//    override fun resumeWith(result: Result<Unit>) {
//        this.result = result
//        val res = try {
//            val r = myFunction(this)
//            if (r == COROUTINE_SUSPENDED) return
//            Result.success(r as Unit)
//        } catch (e: Throwable) {
//            Result.failure(e)
//        }
//        completion.resumeWith(res)
//    }
//}
//
//fun main() {
//    myFunction(EmptyContinuation)
//    Thread.sleep(2000)
//}

//suspend fun myFunction() {
//    println("Before")
//    var counter = 0
//    delay(1000) // suspending
//    counter++
//    println("Counter: $counter")
//    println("After")
//}

//fun myFunction(continuation: Continuation<Unit>): Any {
//    val continuation = continuation as? MyFunctionContinuation
//        ?: MyFunctionContinuation(continuation)
//
//    var counter = continuation.counter
//
//    if (continuation.label == 0) {
//        println("Before")
//        counter = 0
//        continuation.counter = counter
//        continuation.label = 1
//        if (delay(1000, continuation) == COROUTINE_SUSPENDED) {
//            return COROUTINE_SUSPENDED
//        }
//    }
//    if (continuation.label == 1) {
//        counter = (counter as Int) + 1
//        println("Counter: $counter")
//        println("After")
//        return Unit
//    }
//    error("Impossible")
//}
//
//class MyFunctionContinuation(
//    val completion: Continuation<Unit>
//) : Continuation<Unit> {
//    override val context: CoroutineContext
//        get() = completion.context
//
//    var result: Result<Unit>? = null
//    var label = 0
//    var counter = 0
//
//    override fun resumeWith(result: Result<Unit>) {
//        this.result = result
//        val res = try {
//            val r = myFunction(this)
//            if (r == COROUTINE_SUSPENDED) return
//            Result.success(r as Unit)
//        } catch (e: Throwable) {
//            Result.failure(e)
//        }
//        completion.resumeWith(res)
//    }
//}
//
//fun main() {
//    myFunction(EmptyContinuation)
//    Thread.sleep(2000)
//}

//suspend fun printUser(token: String) {
//    println("Before")
//    val userId = getUserId(token) // suspending
//    println("Got userId: $userId")
//    val userName = getUserName(userId, token) // suspending
//    println(User(userId, userName))
//    println("After")
//}
//
//suspend fun getUserId(token: String): String {
//    delay(1000) // suspending
//    return "SomeId"
//}
//
//suspend fun getUserName(userId: String, token: String): String {
//    delay(1000) // suspending
//    return "SomeName"
//}

//data class User(val id: String, val name: String)
//
//fun printUser(token: String, continuation: Continuation<*>): Any {
//    val continuation = continuation as? PrintUserContinuation
//        ?: PrintUserContinuation(continuation as Continuation<Unit>, token)
//
//    var result: Result<Any>? = continuation.result
//    var userId: String? = continuation.userId
//    val userName: String
//
//    if (continuation.label == 0) {
//        println("Before")
//        continuation.label = 1
//        val res = getUserId(token, continuation)
//        if (res == COROUTINE_SUSPENDED) {
//            return COROUTINE_SUSPENDED
//        }
//        result = Result.success(res)
//    }
//    if (continuation.label == 1) {
//        userId = result!!.getOrThrow() as String
//        println("Got userId: $userId")
//        continuation.label = 2
//        continuation.userId = userId
//        val res = getUserName(userId, continuation)
//        if (res == COROUTINE_SUSPENDED) {
//            return COROUTINE_SUSPENDED
//        }
//        result = Result.success(res)
//    }
//    if (continuation.label == 2) {
//        userName = result!!.getOrThrow() as String
//        println(User(userId as String, userName))
//        println("After")
//        return Unit
//    }
//    error("Impossible")
//}
//
//class PrintUserContinuation(
//    val completion: Continuation<Unit>,
//    val token: String
//) : Continuation<String> {
//    override val context: CoroutineContext
//        get() = completion.context
//
//    var label = 0
//    var result: Result<Any>? = null
//    var userId: String? = null
//
//    override fun resumeWith(result: Result<String>) {
//        this.result = result
//        val res = try {
//            val r = printUser(token, this)
//            if (r == COROUTINE_SUSPENDED) return
//            Result.success(r as Unit)
//        } catch (e: Throwable) {
//            Result.failure(e)
//        }
//        completion.resumeWith(res)
//    }
//}
//
//fun main() {
//    printUser("SomeToken", EmptyContinuation)
//    Thread.sleep(3000)    // Needed to prevent the function from finishing immediately.
//}
//
//class ApiException : Throwable("Fake API exception")
//
//fun getUserId(token: String, continuation: Continuation<String>): Any {
//    executor.schedule({ continuation.resume("SomeId") }, 1000, TimeUnit.MILLISECONDS)
//    return COROUTINE_SUSPENDED
//}
//
//fun getUserName(userId: String, continuation: Continuation<String>): Any {
//    executor.schedule({
//        continuation.resume("SomeName")
//        //        continuation.resumeWithException(ApiException())
//    }, 1000, TimeUnit.MILLISECONDS)
//    return COROUTINE_SUSPENDED
//}
//
//private fun Result<*>.throwOnFailure() {
//    if (isFailure) throw exceptionOrNull()!!
//}

//suspend fun a() {
//    val user = readUser()
//    b()
//    b()
//    b()
//    println(user)
//}
//
//suspend fun b() {
//    for (i in 1..10) {
//        c(i)
//    }
//}
//
//suspend fun c(i: Int) {
//    delay(i * 100L)
//    println("Tick")
//}
//
//suspend fun readUser(): User {
//    delay(1000L)
//    return User("SomeUser")
//}
//
//data class User(val name: String)

// Utils

private val executor = Executors
    .newSingleThreadScheduledExecutor {
        Thread(it, "scheduler").apply { isDaemon = true }
    }

private fun delay(timeMillis: Long, continuation: Continuation<Unit>): Any {
    executor.schedule({
        continuation.resume(Unit)
    }, timeMillis, TimeUnit.MILLISECONDS)
    return COROUTINE_SUSPENDED
}

private val COROUTINE_SUSPENDED = Any()

private object EmptyContinuation : Continuation<Unit> {
    override val context: CoroutineContext = EmptyCoroutineContext

    override fun resumeWith(result: Result<Unit>) {
        if (result.isFailure) {
            result.exceptionOrNull()?.printStackTrace()
        }
    }
}
