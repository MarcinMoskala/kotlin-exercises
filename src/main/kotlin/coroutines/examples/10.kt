package examples

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

import kotlinx.coroutines.*

class User()

suspend fun fetchUser(): User {
    // Runs forever
    while (true) {
        yield()
    }
}

suspend fun getUserOrNull(): User? = withTimeoutOrNull(1000) {
    fetchUser()
}

suspend fun main(): Unit = coroutineScope {
    val user = getUserOrNull()
    println("User: $user")
}