package examples

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

suspend fun main() = coroutineScope {
    val dispatcher = Executors.newFixedThreadPool(5)
        .asCoroutineDispatcher()
    repeat(1000) {
        launch(dispatcher) {
            Thread.sleep(200)

            val threadName = Thread.currentThread().name
            println("Running on thread: $threadName")
        }
    }
}