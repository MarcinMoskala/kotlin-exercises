package examples

import kotlinx.coroutines.*
import java.util.concurrent.Executors

var i = 0

val dispatcher = Executors.newSingleThreadExecutor()
    .asCoroutineDispatcher()
// val dispatcher = newSingleThreadContext("My name")

suspend fun main(): Unit = coroutineScope {
    repeat(10_000) {
        launch(Dispatchers.IO) { // or Default
            i++
        }
    }
    delay(1000)
    println(i) // ~9930
}