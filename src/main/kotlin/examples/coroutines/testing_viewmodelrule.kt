package coroutines.examples.testingviewmodelrule

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals

interface UserRepository {
    suspend fun getUser(): UserData
}

interface NewsRepository {
    suspend fun getNews(): List<News>
}

data class UserData(val name: String)
data class News(val date: Date)

interface LiveData<T> {
    val value: T?
}

class MutableLiveData<T> : LiveData<T> {
    override var value: T? = null
}

abstract class ViewModel()

class MainViewModel(
    private val userRepo: UserRepository,
    private val newsRepo: NewsRepository
) : BaseViewModel() {

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName
    private val _news = MutableStateFlow<List<News>?>(null)
    val news: StateFlow<List<News>?> = _news
    private val _showProgress = MutableStateFlow(false)
    val showProgress: StateFlow<Boolean> = _showProgress

    init {
        viewModelScope.launch {
            val user = userRepo.getUser()
            _userName.value = user.name
        }
        viewModelScope.launch {
            _showProgress.value = true
            _news.value = newsRepo.getNews()
                .sortedByDescending { it.date }
            _showProgress.value = false
        }
    }
}

abstract class BaseViewModel : ViewModel() {
    private val context = Dispatchers.Main.immediate + SupervisorJob()
    val viewModelScope = CoroutineScope(context)

    fun onDestroy() {
        context.cancelChildren()
    }
}

class MainViewModelTests {
    @get:Rule
    val dispatcherMainRule = MainCoroutineRule()
    
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        viewModel = MainViewModel(
            userRepo = FakeUserRepository(aName),
            newsRepo = FakeNewsRepository(someNews)
        )
    }

    @Test
    fun `should show progress when loading news`() {
        // given
        assertEquals(false, viewModel.showProgress.value)

        // when
        dispatcherMainRule.scheduler.runCurrent()

        // then
        assertEquals(true, viewModel.showProgress.value)

        // when
        dispatcherMainRule.scheduler.advanceUntilIdle()

        // then
        assertEquals(false, viewModel.showProgress.value)
    }

    @Test
    fun `user name is shown`() {
        // when
        dispatcherMainRule.scheduler.advanceUntilIdle()

        // then
        assertEquals(aName, viewModel.userName.value)
    }

    @Test
    fun `sorted news are shown`() {
        // when
        dispatcherMainRule.scheduler.advanceUntilIdle()

        // then
        val someNewsSorted =
            listOf(News(date1), News(date2), News(date3))
        assertEquals(someNewsSorted, viewModel.news.value)
    }

    @Test
    fun `user and news are called concurrently`() {
        // when
        dispatcherMainRule.scheduler.advanceUntilIdle()

        // then
        assertEquals(1000, dispatcherMainRule.scheduler.currentTime)
    }
}

class FakeUserRepository(val name: String) : UserRepository {
    override suspend fun getUser(): UserData {
        delay(1000)
        return UserData(name)
    }
}

class FakeNewsRepository(val news: List<News>) : NewsRepository {
    override suspend fun getNews(): List<News> {
        delay(1000)
        return news
    }
}

private val date1 = Date
    .from(Instant.now().minusSeconds(10))
private val date2 = Date
    .from(Instant.now().minusSeconds(20))
private val date3 = Date
    .from(Instant.now().minusSeconds(30))

private val aName = "Some name"
private val someNews =
    listOf(News(date3), News(date1), News(date2))

class MainCoroutineRule : TestWatcher() {
    lateinit var scheduler: TestCoroutineScheduler
        private set
    lateinit var dispatcher: TestDispatcher
        private set

    override fun starting(description: Description) {
        scheduler = TestCoroutineScheduler()
        dispatcher = StandardTestDispatcher(scheduler)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
