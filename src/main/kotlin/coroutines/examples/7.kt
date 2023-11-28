package examples

import kotlinx.coroutines.*
import java.util.*

fun main() = runBlocking {
    println("Before")
    supervisorScope {
        launch {
            delay(1000)
            throw Error()
        }
        launch {
            delay(2000)
            println("Done")
        }
    }
    println("After")
}
