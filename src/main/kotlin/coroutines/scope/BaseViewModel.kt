package coroutines.scope.baseviewmodel

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class BaseViewModel : ViewModel() {
    private val _exceptions = Channel<Throwable>(Channel.UNLIMITED)
    val exceptions: Flow<Throwable> = _exceptions.receiveAsFlow()

    val scope: CoroutineScope = TODO()
}

class MainViewModel(
    private val userRepo: UserRepository,
    private val newsRepo: NewsRepository
) : BaseViewModel() {
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    private val _news = MutableStateFlow(emptyList<News>())
    val news: StateFlow<List<News>> = _news

    init {
        scope.launch {
            _userData.value = userRepo.getUser()
        }
        scope.launch {
            _news.value = newsRepo.getNews()
                .sortedByDescending { it.date }
        }
    }
}

abstract class ViewModel {
    open fun onCleared() {}
}

interface UserRepository {
    suspend fun getUser(): UserData
}

interface NewsRepository {
    suspend fun getNews(): List<News>
}

data class UserData(val name: String)
data class News(val date: Date)

class BaseViewModelTests {

    private val UI = newSingleThreadContext("UIThread") // Normally it will be Dispatchers.Main

    @Before
    fun setUp() {
        Dispatchers.setMain(UI)
    }

    @Test
    fun `onDestroy cancels all jobs`() = runTest {
        var jobs = listOf<Job>()
        val viewModel = object : BaseViewModel() {
            init {
                jobs += scope.launch {
                    delay(Long.MAX_VALUE)
                }
                jobs += scope.launch {
                    delay(Long.MAX_VALUE)
                }
            }
        }
        delay(200)
        viewModel.onCleared()
        delay(200)
        assertEquals(listOf(true, true), jobs.map { it.isCancelled })
    }

    @Test
    fun `Coroutines run on main thread`() = runTest {
        var threads = listOf<Thread>()
        val viewModel = object : BaseViewModel() {
            init {
                scope.launch {
                    threads += Thread.currentThread()
                }
            }
        }
        delay(100)
        viewModel.onCleared()
        delay(100)
        threads.forEach {
            assert(it.name.startsWith("UIThread")) { "We should switch to UI thread, and now we are on ${it.name}" }
        }
        assert(threads.isNotEmpty())
    }

    @Test
    fun `When a job throws an error, it is handled`(): Unit = runTest {
        val error1 = Error()
        val error2 = Error()
        val vm = object : BaseViewModel() {
            init {
                scope.launch {
                    throw error1
                }
                scope.launch {
                    throw error2
                }
            }
        }
        var exceptions = setOf<Throwable>()
        vm.exceptions.onEach { exceptions += it }.launchIn(backgroundScope)
        delay(200)
        assertEquals(setOf(error1, error2), exceptions)
    }

    class FakeViewModelForSingleExceptionHandling(val onSecondAction: () -> Unit) : BaseViewModel() {
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
        var started = false
        object : BaseViewModel() {
            init {
                scope.launch {
                    delay(100)
                    throw Error()
                }
                scope.launch {
                    started = true
                    delay(200)
                    called = true
                }
            }
        }
        delay(300)
        assertTrue(started)
        assertTrue(called)
    }
}
