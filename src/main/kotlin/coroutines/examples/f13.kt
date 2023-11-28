package examples

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext

suspend fun main() {
    val UI = newSingleThreadContext("UI")
    val IO = newSingleThreadContext("IO")
    fun logThread(taskName: String) {
        println("Doing $taskName on ${Thread.currentThread().name}")
    }
    withContext(UI) {
        val singleValue =
            flow { logThread("flow"); emit("A") } // will be executed on IO if context wasn't specified before
                .map { logThread("map"); it } // Will be executed in IO
                .flowOn(IO)
                .filter { logThread("filter"); it != null } // Will be executed in Default
                .flowOn(Dispatchers.Default)
                .collect { logThread("collect") }
    }
}