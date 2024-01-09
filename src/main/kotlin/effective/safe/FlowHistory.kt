package effective.safe.flowhistory

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*    
import org.junit.Test
import kotlin.test.assertEquals

fun <T> Flow<T>.withHistory(): Flow<List<T>> = flow {
    val history = mutableListOf<T>()
    emit(history)
    collect {
        history += it
        emit(history)
    }
}

suspend fun main() {
    flowOf(1, 2, 3)
        .withHistory()
        .toList()
        .let(::println)
    // [[1, 2, 3], [1, 2, 3], [1, 2, 3], [1, 2, 3]]
}

class FlowHistoryTests {
    @Test
    fun `should emit empty for empty`() = runTest {
        assertEquals(listOf(listOf()), flowOf<String>().withHistory().toList())
    }

    @Test
    fun `should emit history`() = runTest {
        val flow = flowOf(1, 2, 3).withHistory()
        assertEquals(listOf(listOf(), listOf(1), listOf(1, 2), listOf(1, 2, 3)), flow.toList())
    }

    @Test
    fun `should emit elements as they appear`() = runTest {
        val flow = flow {
            delay(100)
            emit(1)
            delay(1000)
            emit(2)
            delay(10000)
            emit(3)
        }.withHistory()
            .withVirtualTime(this)
        assertEquals(
            listOf(
                ValueAndTime(listOf(), 0),
                ValueAndTime(listOf(1), 100),
                ValueAndTime(listOf(1, 2), 1100),
                ValueAndTime(listOf(1, 2, 3), 11100),
            ), flow.toList()
        )
    }
}

fun <T> Flow<T>.withVirtualTime(testScope: TestScope): Flow<ValueAndTime<T>> =
    map { ValueAndTime(it, testScope.currentTime) }

data class ValueAndTime<T>(val value: T, val timeMillis: Long)
