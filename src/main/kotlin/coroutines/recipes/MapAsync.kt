package coroutines.recipes.mapasync

import kotlinx.coroutines.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertTrue

suspend fun <T, R> Iterable<T>.mapAsync(
    transformation: suspend (T) -> R
): List<R> = TODO()

class MapAsyncTest {
    @Test
    fun should_behave_like_a_regular_map_for_a_list_and_a_set() = runTest {
        val list = ('a'..'z').toList()
        assertEquals(list.map { c -> c.inc() }, list.mapAsync { c -> c.inc() })
        assertEquals(list.map { c -> c.code }, list.mapAsync { c -> c.code })
        assertEquals(list.map { c -> c.uppercaseChar() }, list.mapAsync { c -> c.uppercaseChar() })

        val set = (1..10).toSet()
        assertEquals(set.map { i -> i * i }, set.mapAsync { i -> i * i })
        assertEquals(set.map { i -> "A$i" }, set.mapAsync { i -> "A$i" })
        assertEquals(set.map { i -> i.toChar() }, set.mapAsync { i -> i.toChar() })
    }

    @Test
    fun should_map_async_and_keep_elements_order() = runTest {
        val transforms: List<suspend () -> String> = listOf(
            { delay(3000); "A" },
            { delay(2000); "B" },
            { delay(4000); "C" },
            { delay(1000); "D" },
        )

        val res = transforms.mapAsync { it() }
        assertEquals(listOf("A", "B", "C", "D"), res)
        assertEquals(4000, currentTime)
    }

    @Test
    fun should_support_context_propagation() = runTest {
        var ctx: CoroutineContext? = null

        val name1 = CoroutineName("Name 1")
        withContext(name1) {
            listOf("A").mapAsync {
                ctx = currentCoroutineContext()
                it
            }
        }
        assertEquals(name1, ctx?.get(CoroutineName))

        val name2 = CoroutineName("Some name 2")
        withContext(name2) {
            listOf("B").mapAsync {
                ctx = currentCoroutineContext()
                it
            }
        }
        assertEquals(name2, ctx?.get(CoroutineName))
    }

    @Test
    fun should_support_context_propagation() = runTest {
        var ctx: CoroutineContext? = null

        val name1 = CoroutineName("Name 1")
        withContext(name1) {
            listOf("A").mapAsync {
                ctx = currentCoroutineContext()
                it
            }
        }
        assertEquals(name1, ctx?.get(CoroutineName))

        val name2 = CoroutineName("Some name 2")
        withContext(name2) {
            listOf("B").mapAsync {
                ctx = currentCoroutineContext()
                it
            }
        }
        assertEquals(name2, ctx?.get(CoroutineName))
    }

    @Test
    fun should_propagate_exceptions_from_transformation_and_cancel_other_transformations() = runTest {
        // given
        val e = object : Throwable() {}
        val bodies = listOf(
            suspend { "A" },
            suspend { delay(1000); "B" },
            suspend { delay(500); throw e },
            suspend { "C" }
        )
        val jobs = mutableListOf<CoroutineContext>()

        // when
        val result = runCatching {
            bodies.mapAsync {
                jobs += currentCoroutineContext()
                it()
            }
        }

        // then should propagate exception
        assertTrue(result.isFailure)
        assertEquals(e, result.exceptionOrNull())
        
        // without waiting for slower transformations
        assertEquals(500, currentTime)
        
        // and cancel slower transformations
        assert(jobs.all { it.job.isCompleted })
    }
}
