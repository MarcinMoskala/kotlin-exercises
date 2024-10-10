package coroutines.flow.newsviewmodel

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class NewsViewModel(
    newsRepository: NewsRepository,
) : BaseViewModel() {
    private val _progressVisible = MutableStateFlow(false)
    val progressVisible = _progressVisible.asStateFlow()

    private val _newsToShow = MutableStateFlow(emptyList<News>())
    val newsToShow = _newsToShow.asStateFlow()

    private val _errors = Channel<Throwable>()
    val errors = _errors.receiveAsFlow()

    init {
        // TODO
    }
}

class ApiException : Exception()

interface NewsRepository {
    fun fetchNews(): Flow<News>
}

abstract class BaseViewModel {
    protected val viewModelScope = CoroutineScope(
        Dispatchers.Main.immediate + SupervisorJob()
    )
}

data class News(
    val title: String,
    val description: String,
    val imageUrl: String,
    val url: String,
)

class FakeNewsRepository : NewsRepository {
    val newsList = List(100) { News("Title $it", "Description $it", "ImageUrl $it", "Url $it") }
    var fetchNewsStartDelay = 0L
    var fetchNewsDelay = 0L
    val failWith = mutableListOf<Throwable>()

    override fun fetchNews(): Flow<News> = flow {
        delay(fetchNewsStartDelay)
        failWith.removeFirstOrNull()?.let { throw it }
        newsList.forEach {
            delay(fetchNewsDelay)
            emit(it)
        }
    }
}

class NewsViewModelTest {
    lateinit var dispatcher: TestDispatcher
    lateinit var newsRepository: FakeNewsRepository

    @Before
    fun setUp() {
        newsRepository = FakeNewsRepository()
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should show all news`() {
        val viewModel = NewsViewModel(newsRepository)
        newsRepository.fetchNewsDelay = 1000
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(newsRepository.newsList, viewModel.newsToShow.value)
        assertEquals(newsRepository.newsList.size * newsRepository.fetchNewsDelay, dispatcher.scheduler.currentTime)
    }

    @Test
    fun `should show progress bar when loading news`() {
        val viewModel = NewsViewModel(newsRepository)
        newsRepository.fetchNewsDelay = 1000
        var progressChanges = listOf<Pair<Long, Boolean>>()
        viewModel.progressVisible.onEach {
            progressChanges += dispatcher.scheduler.currentTime to it
        }.launchIn(CoroutineScope(dispatcher))

        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(
            listOf(0L to true, newsRepository.newsList.size * newsRepository.fetchNewsDelay to false),
            progressChanges
        )
    }

    @Test
    fun `should retry API exceptions`() {
        val exceptionsNum = 10
        newsRepository.failWith.addAll(List(exceptionsNum) { ApiException() })
        newsRepository.fetchNewsStartDelay = 1000
        val viewModel = NewsViewModel(newsRepository)
        var errors = listOf<Throwable>()
        viewModel.errors.onEach {
            errors += it
        }.launchIn(CoroutineScope(dispatcher))
        var newsShown = listOf<News>()
        viewModel.newsToShow.onEach {
            newsShown += it
        }.launchIn(CoroutineScope(dispatcher))
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, errors.size)
        assertEquals(newsRepository.newsList, newsShown)
        assertEquals(newsRepository.fetchNewsStartDelay * (exceptionsNum + 1), dispatcher.scheduler.currentTime)
    }

    @Test
    fun `should catch exceptions`() {
        val exception = Exception()
        newsRepository.failWith.add(exception)
        newsRepository.fetchNewsStartDelay = 1000
        val viewModel = NewsViewModel(newsRepository)
        var errors = listOf<Throwable>()
        viewModel.errors.onEach {
            errors += it
        }.launchIn(CoroutineScope(dispatcher))
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(exception), errors)
        assertEquals(false, viewModel.progressVisible.value)
        assertEquals(1000, dispatcher.scheduler.currentTime)
    }
}
