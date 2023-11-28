package examples.e5

import kotlinx.coroutines.*

suspend fun longTask() = coroutineScope {
    launch {
        delay(1000)
        val name = coroutineContext[CoroutineName]?.name
        println("[$name] Finished task 1")
    }
    launch {
        delay(2000)
        val name = coroutineContext[CoroutineName]?.name
        println("[$name] Finished task 2")
    }
}

fun main() = runBlocking(CoroutineName("Parent")) {
    println("Before")
    longTask()

//    val job = launch(CoroutineName("Parent")) {
//        longTask()
//    }
//    launch {
//        delay(1500)
//        job.cancel()
//    }

    println("After")
}