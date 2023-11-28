package examples

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    (1..10).asFlow()
        .scan(0) { acc, v -> acc + v }
        .collect { println(it) }
}
