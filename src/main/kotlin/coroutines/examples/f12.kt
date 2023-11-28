package examples

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

suspend fun main() {
    val nums = (1..3).asFlow().onEach { delay(300) }
    val strs = flowOf("one", "two", "three").onEach { delay(400) }

    println("Zip:")
    val startTime = ct
    nums.zip(strs) { a, b -> "$a -> $b" }
        .collect { value ->
            println("$value at ${ct - startTime} ms")
        }

    println("Combine:")
    val startTime2 = ct
    nums.combine(strs) { a, b -> "$a -> $b" }
        .collect { value ->
            println("$value at ${ct - startTime2} ms")
        }
}

val ct: Long get() = System.currentTimeMillis()