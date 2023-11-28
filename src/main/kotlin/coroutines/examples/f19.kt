package examples

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    flowOf(1, 2, 3)
        .map { it / 0 }
        .catch { emit(-1) }
        .onEach { it / 0 }
        .catch { print("Got it") }
        .collect()
}