package coroutines.flow.flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

val infiniteFlow: Flow<Unit> = TODO()

val neverFlow: Flow<Nothing> = TODO()

fun everyFlow(timeMillis: Long): Flow<Unit> = TODO()

fun <T> flowOf(lambda: suspend () -> T): Flow<T> = TODO()

fun <T> flowOfFlatten(
    lambda: suspend () -> Flow<T>
): Flow<T> = TODO()

class FlowUtilsTest {
    @Test
    fun `should create infinite flow`() = runTest {
        val result = infiniteFlow.take(10).onEach { delay(1000) }.toList()
        assertEquals(listOf(Unit, Unit, Unit, Unit, Unit, Unit, Unit, Unit, Unit, Unit), result)
        assertEquals(10_000, currentTime)
    }

    @Test
    fun `should create never flow`() = runTest {
        var emitted = false
        var completed = false
        neverFlow.onEach {
            emitted = true
        }.onCompletion { 
            completed = true
        }.launchIn(backgroundScope)
        delay(Long.MAX_VALUE - 1)
        assertEquals(false, emitted)
        assertEquals(false, completed)
        assertEquals(Long.MAX_VALUE - 1, currentTime)
    }

    @Test
    fun `should create every flow`() = runTest {
        val result = everyFlow(1000).take(10).toList()
        assertEquals(listOf(Unit, Unit, Unit, Unit, Unit, Unit, Unit, Unit, Unit, Unit), result)
        assertEquals(10_000, currentTime)
    }

    @Test
    fun `should create flow of`() = runTest {
        assertEquals(listOf(1), flowOf { 1 }.toList())
        assertEquals(listOf("A"), flowOf { "A" }.toList())
        assertEquals(0, currentTime)
        assertEquals(listOf(1), flowOf { delay(1000); 1 }.toList())
        assertEquals(1000, currentTime)
    }

    @Test
    fun `should create flow of flatten`() = runTest {
        assertEquals(listOf(1), flowOfFlatten { flowOf(1) }.toList())
        assertEquals(listOf("A"), flowOfFlatten { flowOf("A") }.toList())
        assertEquals(0, currentTime)
        val flow: List<ValueAndTime<Int>> = flowOfFlatten {
            delay(1000)
            flow {
                repeat(10) {
                    delay(1000)
                    emit(it)
                }
            }
        }.withVirtualTime(this)
            .toList()
        assertEquals(
            listOf(
                ValueAndTime(0, 2000),
                ValueAndTime(1, 3000),
                ValueAndTime(2, 4000),
                ValueAndTime(3, 5000),
                ValueAndTime(4, 6000),
                ValueAndTime(5, 7000),
                ValueAndTime(6, 8000),
                ValueAndTime(7, 9000),
                ValueAndTime(8, 10000),
                ValueAndTime(9, 11000),
            ),
            flow
        )
    }
}

fun <T> Flow<T>.withVirtualTime(testScope: TestScope): Flow<ValueAndTime<T>> =
    map { ValueAndTime(it, testScope.currentTime) }

data class ValueAndTime<T>(val value: T, val timeMillis: Long)
