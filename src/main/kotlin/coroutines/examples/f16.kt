package examples

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun main() = measureTimeMillis {
    runBlocking {
        measureTimeMillis {
            (1..5).asFlow()
                .onEach { event -> delay(100) }
                .launchIn(this)

            (1..5).asFlow()
                .onEach { event -> delay(100) }
                .launchIn(this)

        }.let(::print) // 15
    }
}.let(::print) // 591
