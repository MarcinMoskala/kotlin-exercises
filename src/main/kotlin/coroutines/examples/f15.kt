package examples

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun main() = runBlocking<Unit> {
    measureTimeMillis {
        (1..5).asFlow()
            .onEach { event -> delay(100) }
            .collect() // We wait 500ms

        (1..5).asFlow()
            .onEach { event -> delay(100) }
            .collect() // We wait 500ms

    }.let(::print) // 1049
}