package advanced.contract.mesurecoroutinetime

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.coroutineContext
import kotlin.system.measureTimeMillis
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

suspend fun measureCoroutine(
    body: suspend () -> Unit
): Duration {
    val dispatcher = coroutineContext[ContinuationInterceptor]
    return if (dispatcher is TestDispatcher) {
        val before = dispatcher.scheduler.currentTime
        body()
        val after = dispatcher.scheduler.currentTime
        after - before
    } else {
        measureTimeMillis {
            body()
        }
    }.milliseconds
}

suspend fun main() {
    //runTest {
    //    val result: String
    //    val duration = measureCoroutine {
    //        delay(1000)
    //        result = "OK"
    //    }
    //    println(duration) // 1000 ms
    //    println(result) // OK
    //}
    //
    //runBlocking {
    //    val result: String
    //    val duration = measureCoroutine {
    //        delay(1000)
    //        result = "OK"
    //    }
    //    println(duration) // 1000 ms
    //    println(result) // OK
    //}
}
