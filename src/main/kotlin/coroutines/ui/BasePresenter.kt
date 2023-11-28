package ui

import kotlinx.coroutines.*
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class BasePresenter(
    private val onError: (Throwable) -> Unit = {}
) {
    val scope: CoroutineScope = TODO()

    fun onDestroy() {}
}

@Suppress("FunctionName")
class BasePresenterTests {

    private val UI = newSingleThreadContext("UIThread") // Normally it will be Dispatchers.Main

    @Before
    fun setUp() {
        Dispatchers.setMain(UI)
    }

    class FakePresenter(
        private val jobInterceptor: (() -> Unit)? = null,
        onError: (Throwable) -> Unit = {}
    ) : BasePresenter(onError) {

        var cancelledJobs = 0

        fun onCreate() {
            scope.launch {
                try {
                    delay(100)
                    jobInterceptor?.invoke()
                    delay(2000)
                } finally {
                    cancelledJobs += 1
                }
            }
            scope.launch {
                try {
                    delay(100)
                    jobInterceptor?.invoke()
                    delay(2000)
                } finally {
                    cancelledJobs += 1
                }
            }
        }
    }

    @Test
    fun `onDestroy cancels all jobs`() = runBlocking {
        val presenter = FakePresenter()
        presenter.onCreate()
        delay(200)
        presenter.onDestroy()
        delay(200)
        assertEquals(2, presenter.cancelledJobs)
    }

    @Test
    fun `Coroutines run on main thread`() = runBlocking {
        var threads = listOf<Thread>()
        val presenter = FakePresenter(
            jobInterceptor = {
                threads += Thread.currentThread()
            }
        )
        presenter.onCreate()
        delay(100)
        presenter.onDestroy()
        delay(100)
        threads.forEach {
            assert(it.name.startsWith("UIThread")) { "We should switch to UI thread, and now we are on ${it.name}" }
        }
        assert(threads.isNotEmpty())
    }

    @Test
    fun `When a job throws an error, it is handled`(): Unit = runBlocking {
        val error = Error()
        var errors = listOf<Throwable>()
        val presenter = FakePresenter(
            jobInterceptor = { throw error },
            onError = { errors += it }
        )
        presenter.onCreate()
        delay(200)
        assertEquals(error, errors.first())
    }

    class FakePresenterForSingleExceptionHandling(val onSecondAction: () -> Unit) : BasePresenter() {

        var cancelledJobs = 0

        fun onCreate() {
            scope.launch {
                delay(100)
                throw Error()
            }
            scope.launch {
                delay(200)
                onSecondAction()
            }
        }
    }

    @Test
    fun `Error on a single coroutine, does not cancel others`() = runBlocking {
        var called = false
        val presenter = FakePresenterForSingleExceptionHandling(
            onSecondAction = { called = true }
        )
        presenter.onCreate()
        delay(300)
        assertTrue(called)
    }
}
