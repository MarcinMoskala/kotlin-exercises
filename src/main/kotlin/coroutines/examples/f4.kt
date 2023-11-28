package examples

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    flowOf(1, 2, 3)
        .filter { it % 2 == 1 }
        .collect { print(it) } // 13
}
