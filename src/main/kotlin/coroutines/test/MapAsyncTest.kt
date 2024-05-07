package coroutines.test.mapasynctest

import kotlinx.coroutines.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertTrue

suspend fun <T, R> Iterable<T>.mapAsync(
    transformation: suspend (T) -> R
): List<R> = coroutineScope {
    map { async { transformation(it) } }
        .awaitAll()
}

class MapAsyncTest {
    @Test
    fun `should behave like a regular map`() = runTest {
        // TODO
    }

    @Test
    fun `should map async`() = runTest {
        // TODO
    }

    @Test
    fun `should keep elements order`() = runTest {
        // TODO
    }

    @Test
    fun `should support context propagation`() = runTest {
        // TODO
    }

    @Test
    fun `should support cancellation`() = runTest {
        // TODO
    }

    @Test
    fun `should propagate exceptions`() = runTest {
        // TODO
    }
}
