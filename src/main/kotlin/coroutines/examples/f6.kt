package examples

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.system.measureTimeMillis

suspend fun main() {
    measureTimeMillis {
        ('A'..'C').asFlow()
            .flatMapConcat { flowFrom(it) }
            .collect { print(it) } // 0_A 1_A 2_A 0_B 1_B 2_B 0_C 1_C 2_C
    }.let(::print) // 9060
}

fun flowFrom(elem: Any) = flowOf(0, 1, 2)
    .onEach { delay(1000) }
    .map { "${it}_${elem} " }
