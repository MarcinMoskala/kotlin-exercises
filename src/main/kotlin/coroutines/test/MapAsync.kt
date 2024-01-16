package coroutines.test.mapasync

import kotlinx.coroutines.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals

suspend fun <T, R> Iterable<T>.mapAsync(
    transformation: suspend (T) -> R
): List<R> = coroutineScope { 
    this@mapAsync.map { async { transformation(it) } }
        .awaitAll()
}

class MapAsyncTest {
    @Test
    fun should_behave_like_a_regular_map_for_a_list_and_a_set() = runTest {
       
    }

    @Test
    fun should_map_async_and_keep_elements_order() = runTest {
        
    }

    @Test
    fun should_support_context_propagation() = runTest {
        
    }

    @Test
    fun should_support_cancellation() = runTest {
        
    }
}
