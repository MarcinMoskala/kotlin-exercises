package examples

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

var counter = 0

fun main() = runBlocking {
    massiveRun {
        counter++
    }
    println("Counter = $counter")
}

suspend fun massiveRun(action: suspend () -> Unit) = withContext(Dispatchers.Default) {
    List(1000) {
        launch {
            repeat(1000) { action() }
        }
    }
}
