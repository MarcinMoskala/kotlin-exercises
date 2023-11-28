package examples

import kotlinx.coroutines.*

fun main() = runBlocking {
    val job = Job()
    launch(job) {
        repeat(5) { num ->
            delay(200)
            println("Rep$num")
        }
    }
    launch {
        delay(500)
        job.complete()
    }
    job.join()
    launch(job) {
        println("Will not be printed")
    }
    println("Done")
}