package callback

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FetchTasksUseCase(
    private val callbackUseCase: FetchTasksCallbackUseCase
) {
    @Throws(ApiException::class)
    suspend fun fetchTasks(): List<Task> = TODO()
    suspend fun fetchTasksResult(): Result<List<Task>> = TODO()
    suspend fun fetchTasksOrNull(): List<Task>? = TODO()
}

interface FetchTasksCallbackUseCase {
    fun fetchTasks(onSuccess: (List<Task>) -> Unit, onError: (Throwable) -> Unit): Cancellable
}

fun interface Cancellable {
    fun cancel()
}
data class Task(val name: String, val priority: Int)
class ApiException(val code: Int, message: String): Throwable(message)

@OptIn(ExperimentalCoroutinesApi::class)
class FetchTasksTests {
    val someTasks = listOf(Task("1", 123), Task("2", 456))
    val someException = ApiException(500, "Some exception")

    @Test
    fun `fetchTasks should resume with result`() = runTest {
        // given
        val fakeFetchTaskCallback = FakeFetchTasksCallbackUseCase()
        val useCase = FetchTasksUseCase(fakeFetchTaskCallback)
        var result: List<Task>? = null

        // when
        launch {
            result = useCase.fetchTasks()
        }

        // then
        runCurrent()
        assertEquals(null, result)
        fakeFetchTaskCallback.onSuccess?.invoke(someTasks)
        runCurrent()
        assertEquals(someTasks, result)
    }

    @Test
    fun `fetchTasks should resume with exception`() = runTest {
        // given
        val fakeFetchTaskCallback = FakeFetchTasksCallbackUseCase()
        val useCase = FetchTasksUseCase(fakeFetchTaskCallback)
        var exception: Throwable? = null

        // when
        launch {
            try {
                useCase.fetchTasks()
            } catch (e: Throwable) {
                exception = e
            }
        }

        // then
        runCurrent()
        assertEquals(null, exception)
        fakeFetchTaskCallback.onError?.invoke(someException)
        runCurrent()
        assertEquals(someException, exception)
    }

    @Test
    fun `fetchTasks should support cancellation`() = runTest {
        // given
        val fakeFetchTaskCallback = FakeFetchTasksCallbackUseCase()
        val useCase = FetchTasksUseCase(fakeFetchTaskCallback)
        var cancelled = false
        fakeFetchTaskCallback.onCancelled = { cancelled = true }

        // when
        val job = launch {
            useCase.fetchTasks()
        }

        // then
        runCurrent()
        assertEquals(false, cancelled)
        job.cancel()
        assertEquals(true, cancelled)
    }

    @Test
    fun `fetchTasksResult should resume with result`() = runTest {
        // given
        val fakeFetchTaskCallback = FakeFetchTasksCallbackUseCase()
        val useCase = FetchTasksUseCase(fakeFetchTaskCallback)
        var result: Result<List<Task>>? = null

        // when
        launch {
            result = useCase.fetchTasksResult()
        }

        // then
        runCurrent()
        assertEquals(null, result)
        fakeFetchTaskCallback.onSuccess?.invoke(someTasks)
        runCurrent()
        assertNotNull(result)
        assertTrue(result!!.isSuccess)
        assertEquals(someTasks, result!!.getOrNull())
    }

    @Test
    fun `fetchTasksResult should resume with failure`() = runTest {
        // given
        val fakeFetchTaskCallback = FakeFetchTasksCallbackUseCase()
        val useCase = FetchTasksUseCase(fakeFetchTaskCallback)
        var result: Result<List<Task>>? = null

        // when
        launch {
            result = useCase.fetchTasksResult()
        }

        // then
        runCurrent()
        assertEquals(null, result)
        fakeFetchTaskCallback.onError?.invoke(someException)
        runCurrent()
        assertNotNull(result)
        assertTrue(result!!.isFailure)
        assertEquals(someException, result!!.exceptionOrNull())
    }

    @Test
    fun `fetchTasksResult should support cancellation`() = runTest {
        // given
        val fakeFetchTaskCallback = FakeFetchTasksCallbackUseCase()
        val useCase = FetchTasksUseCase(fakeFetchTaskCallback)
        var cancelled = false
        fakeFetchTaskCallback.onCancelled = { cancelled = true }

        // when
        val job = launch {
            useCase.fetchTasksResult()
        }

        // then
        runCurrent()
        assertEquals(false, cancelled)
        job.cancel()
        assertEquals(true, cancelled)
    }

    @Test
    fun `fetchTasksOrNull should resume with result`() = runTest {
        // given
        val fakeFetchTaskCallback = FakeFetchTasksCallbackUseCase()
        val useCase = FetchTasksUseCase(fakeFetchTaskCallback)
        val NO_VALUE = Any()
        var result: Any? = NO_VALUE

        // when
        launch {
            result = useCase.fetchTasksOrNull()
        }

        // then
        runCurrent()
        assertEquals(NO_VALUE, result)
        fakeFetchTaskCallback.onSuccess?.invoke(someTasks)
        runCurrent()
        assertEquals(someTasks, result)
    }

    @Test
    fun `fetchTasksOrNull should resume with failure`() = runTest {
        // given
        val fakeFetchTaskCallback = FakeFetchTasksCallbackUseCase()
        val useCase = FetchTasksUseCase(fakeFetchTaskCallback)
        val NO_VALUE = Any()
        var result: Any? = NO_VALUE

        // when
        launch {
            result = useCase.fetchTasksOrNull()
        }

        // then
        runCurrent()
        assertEquals(NO_VALUE, result)
        fakeFetchTaskCallback.onError?.invoke(someException)
        runCurrent()
        assertEquals(null, result)
    }

    @Test
    fun `fetchTasksOrNull should support cancellation`() = runTest {
        // given
        val fakeFetchTaskCallback = FakeFetchTasksCallbackUseCase()
        val useCase = FetchTasksUseCase(fakeFetchTaskCallback)
        var cancelled = false
        fakeFetchTaskCallback.onCancelled = { cancelled = true }

        // when
        val job = launch {
            useCase.fetchTasksOrNull()
        }

        // then
        runCurrent()
        assertEquals(false, cancelled)
        job.cancel()
        assertEquals(true, cancelled)
    }

    class FakeFetchTasksCallbackUseCase: FetchTasksCallbackUseCase {
        var onSuccess: ((List<Task>) -> Unit)? = null
        var onError: ((Throwable) -> Unit)? = null
        var onCancelled: (()->Unit)? = null

        override fun fetchTasks(onSuccess: (List<Task>) -> Unit, onError: (Throwable) -> Unit): Cancellable {
            this.onSuccess = onSuccess
            this.onError = onError
            return Cancellable { onCancelled?.invoke() }
        }
    }
}