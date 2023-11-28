package examples

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlin.system.measureTimeMillis

suspend fun main() {
    measureTimeMillis {
        ('A'..'C').asFlow()
            .flatMapMerge(concurrency = 2) { flowFrom(it) }
            .collect { print(it) } // 0_A 0_B 1_B 1_A 2_B 2_A 0_C 1_C 2_C
    }.let(::print) // 6129
}