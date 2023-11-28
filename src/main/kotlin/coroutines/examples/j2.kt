package examples

import kotlinx.coroutines.*

fun main(): Unit = runBlocking {
    val job: Job = launch {
        delay(1000)
        println("Test")
    }

    val ret: Deferred<String> = async {
        delay(1000)
        "Test"
    }
    val job2: Job = ret
}

