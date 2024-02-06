package coroutines.flow.distinct

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test

fun <T> Flow<T>.distinct(): Flow<T> = TODO()

class DistinctTest {
    @Test
    fun `should remove duplicates`() = runTest {
        val flow = flowOf(1, 3, 1, 2, 3, 1, 2, 3)
        val distinctFlow = flow.distinct()
        assertEquals(listOf(1, 3, 2), distinctFlow.toList())
    }

    @Test
    fun `should not introduce unnecessary delays`() = runTest {
        val f = flowOf(1, 1, 3, 1, 2, 3, 1, 2, 3, 1)
        val f1 = f
            .onEach { delay(100) }
            .distinct()
            .map { currentTime to it }
            .toList()
        assertEquals(listOf(100L to 1, 300L to 3, 500L to 2), f1)
        assertEquals(1000L, currentTime)
        val f2 = f
            .distinct()
            .onEach { delay(100) }
            .map { currentTime to it }
            .toList()
        assertEquals(listOf(1100L to 1, 1200L to 3, 1300L to 2), f2)
    }

    @Test
    fun `should keep data flow-specific`() = runTest {
        val f = flowOf(1, 1, 3, 1, 2, 3, 1, 2, 3, 1)
            .distinct()

        assertEquals(listOf(1, 3, 2), f.toList())
        assertEquals(listOf(1, 3, 2), f.toList())
    } 
}
