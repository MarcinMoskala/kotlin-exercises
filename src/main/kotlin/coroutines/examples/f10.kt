package examples

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach

suspend fun main() = coroutineScope<Unit> {
    flowOf("A", "B", "C")
        .onEach { println("onEach $it") }
        .collect { println("collect $it") }
}

//suspend fun main() = coroutineScope<Unit> {
//    flowOf("A", "B", "C")
//            .onEach  { println("onEach $it") }
//            .buffer(100)
//            .collect { println("collect $it") }
//}
