package examples

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlin.system.measureTimeMillis

suspend fun main() {
    measureTimeMillis {
        ('A'..'C').asFlow()
            .onEach { delay(1500) }
            .flatMapLatest { flowFrom(it) }
            .collect { print(it) } // 0_A 0_B 0_C 1_C 2_C
    }.let(::print) // 7656
}
