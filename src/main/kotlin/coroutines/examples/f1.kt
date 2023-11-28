package examples

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    flowOf(1,2,3)
        .collect { print(it) }
}
