package effective.safe.flowhistory

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import utils.ValueAndTime
import utils.withVirtualTime
import kotlin.test.assertEquals

fun <T> Flow<T>.withHistory(): Flow<List<T>> = flow {
    val history = mutableListOf<T>()
    emit(history)
    collect {
        history.add(it)
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
