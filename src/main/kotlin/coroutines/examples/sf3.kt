import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

suspend fun main() = coroutineScope {
    val state = MutableStateFlow(1)
    println(state.value) // 1

    delay(1000)

    launch {
        state.collect { println("Value changed to $it") } // Value changed to 1
    }

    delay(1000)

    state.value = 2 // Value changed to 2

    delay(1000)

    launch {
        state.collect { println("and now it is $it") } // and now it is 2
    }

    delay(1000)

    state.value = 3 // Value changed to 3 and now it is 3
}
