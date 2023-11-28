package examples

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip

suspend fun main() {
    val f1 = flowOf(1, 2, 3).onEach { delay(1000) }
    val f2 = flowOf("A", "B", "C").onEach { delay(800) }

    f1.zip(f2) { t1, t2 -> t2 + t1 }
        .collect { print("$it ") } // A1 B2 C3

    f1.combine(f2) { t1, t2 -> t2 + t1 }
        .collect { print("$it ") } // A1 B1 B2 C2 C3

    flowOf(f1, f2).flattenConcat()
        .collect { print("$it ") } // 1 2 3 A B C

    flowOf(f1, f2).flattenMerge()
        .collect { print("$it ") } // A 1 B 2 C 3
}