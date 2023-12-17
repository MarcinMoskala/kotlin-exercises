package coroutines.suspension

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

var continuation: Continuation<String>? = null

suspend fun continuationSteal(console: Console) {
    console.println("Before")
    // TODO: Suspend in here and store continuation in the `continuation` variable.
    // USE suspendCancellableCoroutine instead of suspendCoroutine
    // TODO: After continuation resume, print using `console` the value that was passed.
    console.println("After")
}

interface Console {
    fun println(text: Any?)
}

fun main(): Unit = runBlocking<Unit> {
    launch {
        continuationSteal(object : Console {
            override fun println(text: Any?) {
                kotlin.io.println(text)
            }
        })
    }
    delay(1000)
    continuation?.resume("This is some text")
}

@Suppress("FunctionName")
class ContinuationStealTests {

    private val fakeText = "This is some text"

    class FakeConsole : Console {
        val printed = mutableListOf<Any?>()

        override fun println(text: Any?) {
            printed += text
        }
    }

    @Test
    fun `At the beginning function says Before`() = runTest(UnconfinedTestDispatcher()) {
        val fakeConsole = FakeConsole()
        val job = launch {
            continuationSteal(fakeConsole)
        }
        delay(100)
        assertEquals("Before", fakeConsole.printed.first())
        job.cancel()
    }

    @Test
    fun `At the end function says After`() = runTest(UnconfinedTestDispatcher()) {
        val fakeConsole = FakeConsole()
        val job = launch {
            continuationSteal(fakeConsole)
        }
        continuation?.resume(fakeText)
        assertEquals("After", fakeConsole.printed.last())
    }

    @Test
    fun `In the middle, we suspend function`() = runTest(UnconfinedTestDispatcher()) {
        val fakeConsole = FakeConsole()
        val job = launch {
            continuationSteal(fakeConsole)
        }
        assertEquals(mutableListOf<Any?>("Before"), fakeConsole.printed)
        job.cancel()
    }

    @Test
    fun `Function should return continuation`() = runTest(UnconfinedTestDispatcher()) {
        val fakeConsole = FakeConsole()
        launch {
            continuationSteal(fakeConsole)
        }
        assertNotNull(continuation).resume(fakeText)
        assertEquals("After", fakeConsole.printed.last())
    }

    @Test
    fun `Only Before is printed before resume`() = runTest(UnconfinedTestDispatcher()) {
        val fakeConsole = FakeConsole()
        val job = launch {
            continuationSteal(fakeConsole)
        }
        assertEquals("Before", fakeConsole.printed.first())
        job.cancel()
    }

    @Test
    fun `After resume function should print text to resume`() = runTest(UnconfinedTestDispatcher()) {
        val fakeConsole = FakeConsole()
        launch {
            continuationSteal(fakeConsole)
        }
        continuation?.resume(fakeText)
        assertEquals(3, fakeConsole.printed.size)
        assertEquals(fakeText, fakeConsole.printed[1])
    }
}
