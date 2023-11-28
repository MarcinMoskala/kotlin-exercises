package examples

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    flowOf(1, 2, 3)
        .onEach { print(it) } // 123
        .map { it * 10 }
        .collect { print(it) } // 102030
}