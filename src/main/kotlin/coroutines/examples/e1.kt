package examples.c1

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    launch {
        launch {
            delay(1_000)
            throw Error()
        }
        launch {
            delay(2_000)
            println("Done")
        }
    }
    launch {
        delay(3_000)
        println("Done2")
    }
}
