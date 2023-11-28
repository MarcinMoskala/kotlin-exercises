package examples

import kotlinx.coroutines.*

fun main(): Unit = runBlocking {
    launch {
        delay(1000)
        println("Test1")
    }
    launch {
        delay(1000)
        println("Test2")
    }

    coroutineContext[Job]
        ?.children
        ?.forEach { it.join() }
    println("All tests are done")
}