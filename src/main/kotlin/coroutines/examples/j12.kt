package examples

import kotlinx.coroutines.*
import kotlin.random.Random

suspend fun main(): Unit = coroutineScope {
    val job = Job()
    launch(job) {
        try {
            delay(200)
            println("Job is done")
        } finally {
            println("Finally")
            launch { // will be ignored
                println("Will not be printed")
            }
            delay(100) // here exception is thrown
            println("Will not be printed")
        }
    }
    delay(100)
    job.cancelAndJoin()
    println("Cancel done")
}