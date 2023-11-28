package examples.n2

import examples.massiveRun
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

private var counter = 0

fun main() = runBlocking {
    val counterContext = Executors.newSingleThreadExecutor()
        .asCoroutineDispatcher()

    massiveRun {
        withContext(counterContext) {
            counter++
        }
    }
    println("Counter = $counter")
}
