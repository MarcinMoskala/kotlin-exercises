package examples

import kotlinx.coroutines.*

suspend fun main(): Unit = coroutineScope {
    launch {
        launch {
            delay(2000)
            println("Will not be printed")
        }
        withTimeout(1000) {
            delay(1500)
        }
    }
    launch {
        delay(2000)
        println("Done")
    }
}
