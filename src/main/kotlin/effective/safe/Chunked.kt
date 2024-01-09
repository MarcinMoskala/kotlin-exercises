package effective.safe.chunked

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertIs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun <T> Flow<T>.chunked(
    duration: Duration
): Flow<List<T>> = TODO()

class ChunkedTest {
    @Test
    fun `should emit values and complete when original flow completes`() = runTest {
        val actual = (1..5).asFlow()
            .onEach { delay(it.toLong()) }
            .chunked(100.milliseconds)
            .withVirtualTime(this)
            .toList()
        val expected = listOf(
            ValueAndTime(listOf(1, 2, 3, 4, 5), 15),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `should not emit empty chunks`() = runTest {
        val actual = flow {
            emit(1)
            delay(105)
            emit(2)
            delay(100)
        }
            .chunked(10.milliseconds)
            .withVirtualTime(this)
            .toList()
        val expected = listOf(
            ValueAndTime(listOf(1), 10),
            ValueAndTime(listOf(2), 110),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `should stop observing when observer is cancelled`() = runTest {
        val source = MutableSharedFlow<Int>()

        val job = launch {
            source.chunked(100.milliseconds).collect()
        }

        delay(1)
        assertEquals(1, source.subscriptionCount.value)
        job.cancel()
        delay(1)
        assertEquals(0, source.subscriptionCount.value)
    }

    @Test
    fun `should pass exception from source flow`() = runTest {
        class TestException : RuntimeException()

        val source = flow {
            emit(1)
            throw TestException()
        }

        val result = try {
            source.chunked(100.milliseconds).toList()
            null
        } catch (e: Throwable) {
            e
        }

        assertIs<TestException>(result)
    }

    @Test
    fun `should chunk example flow`() = runTest {
        val actual = (1..30).asFlow()
            .onEach { delay(it.toLong()) }
            .chunked(100.milliseconds)
            .withVirtualTime(this)
            .toList()
        val expected = listOf(
            ValueAndTime(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13), 100),
            ValueAndTime(listOf(14, 15, 16, 17, 18, 19), 200),
            ValueAndTime(listOf(20, 21, 22, 23), 300),
            ValueAndTime(listOf(24, 25, 26, 27), 400),
            ValueAndTime(listOf(28, 29, 30), 465),
        )
        assertEquals(expected, actual)
    }
}

fun <T> Flow<T>.withVirtualTime(testScope: TestScope): Flow<ValueAndTime<T>> =
    map { ValueAndTime(it, testScope.currentTime) }

data class ValueAndTime<T>(val value: T, val timeMillis: Long)
