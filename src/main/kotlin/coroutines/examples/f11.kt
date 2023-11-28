package examples

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList

suspend fun main() = coroutineScope {
    val flow = flow {
        for (i in 1..30) {
            delay(10)
            emit(i)
        }
    }

    print(flow.onEach { delay(100) }.toList())
    // [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30]

    print(flow.conflate().onEach { delay(100) }.toList())
    // [1, 10, 20, 30]
}
