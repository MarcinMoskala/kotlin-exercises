//package coroutines.test
//
//import kotlinx.coroutines.*
//import kotlinx.coroutines.test.currentTime
//import kotlinx.coroutines.test.runTest
//import org.junit.After
//import org.junit.Assert.assertEquals
//import kotlin.test.Test
//
//class SuspendingCache<K : Any, V> {
//    private val cache = mutableMapOf<K, Deferred<V>>()
//    private val lock = Any()
//
//    suspend fun get(
//        key: K,
//        build: suspend (key: K) -> V
//    ): V = coroutineScope {
//        val deferred = synchronized(lock) {
//            val deferred = cache[key]
//            if (deferred != null && !deferred.isCancelled) {
//                deferred
//            } else {
//                val newDeferred = async {
//                    build(key)
//                }
//                cache[key] = newDeferred
//                newDeferred
//            }
//        }
//        try {
//            deferred.await()
//        } catch (_: Throwable) {
//            ensureActive()
//            get(key, build)
//        }
//    }
//
//    fun invalidateAll() = synchronized(lock) {
//        cache.forEach { (_, deferred) ->
//            deferred.cancel()
//        }
//        cache.clear()
//    }
//}
//
//class SuspendingCacheTest {
//
//    private val cache = SuspendingCache<String, String>()
//
//    @After
//    fun clear() {
//        cache.invalidateAll()
//    }
//
//    @Test
//    fun `should return value from cache`() = runTest {
//        val value = cache.get("key") { "value" }
//        assertEquals("value", value)
//    }
//
//    @Test
//    fun `should return value from cache after suspending`() = runTest {
//        val value = cache.get("key") { "value" }
//        assertEquals("value", value)
//        val value2 = cache.get("key") { "value2" }
//        assertEquals("value", value2)
//    }
//
//    @Test
//    fun `should not make unnecessary calls`() = runTest {
//        var calls = 0
//        suspend fun request(key: String): String {
//            calls++
//            delay(1000)
//            return "Result for $key"
//        }
//
//        val result1 = cache.get("ABC", ::request)
//        assertEquals(1000L, currentTime)
//        assertEquals("Result for ABC", result1)
//
//        val result2 = cache.get("ABC", ::request)
//        assertEquals(1000L, currentTime)
//        assertEquals("Result for ABC", result2)
//
//        val result3 = cache.get("DEF", ::request)
//        assertEquals(2000L, currentTime)
//        assertEquals("Result for DEF", result3)
//        assertEquals(2, calls)
//    }
//
//    @Test
//    fun `should try for other callers whan one fails`() = runTest {
//        val result1 = runCatching {
//            cache.get("key") {
//                delay(1000)
//                error("Test error")
//            }
//        }
//        val result2 = cache.get("key") { "ABC" }
//        assertEquals(true, result1.isFailure)
//        assertEquals("ABC", result2)
//        assertEquals(1000, currentTime)
//    }
//
//    @Test
//    fun `should first caller cancellation not cancel other`() = runTest {
//        val callerJob = launch {
//            cache.get("key") {
//                delay(1000)
//                "ABCD"
//            }
//        }
//        val otherDef = (1..5).map { i ->
//            async {
//                cache.get("key") {
//                    delay(1000)
//                    "Res$i"
//                }
//            }
//        }
//        callerJob.cancel()
//        val result = otherDef.awaitAll().toList()
//
//        assertEquals(listOf("Res1", "Res1", "Res1", "Res1", "Res1"), result)
//        assertEquals(1000, currentTime)
//    }
//
//    @Test
//    fun `should throw exception when failed`() = runTest {
//        val exception: Throwable = object : Exception() {}
//        val result = runCatching {
//            cache.get("key") {
//                throw exception
//            }
//        }
//        assertEquals(exception, result.exceptionOrNull())
//    }
//
//    @Test
//    fun `should retry after failing request`() = runTest {
//        val exception: Throwable = object : Exception() {}
//
//        val result1 = runCatching {
//            cache.get("key") {
//                throw exception
//            }
//        }
//        delay(1000)
//        assertEquals(exception, result1.exceptionOrNull())
//
//        val result2 = runCatching {
//            cache.get("key") {
//                "ABC"
//            }
//        }
//        assertEquals("ABC", result2.getOrNull())
//    }
//}
