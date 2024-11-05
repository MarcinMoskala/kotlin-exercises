package coroutines.examples.testingviewmodel

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
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

abstract class ViewModel()

class MainViewModel(
    private val userRepo: UserRepository,
    private val newsRepo: NewsRepository
) : BaseViewModel() {

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName
    private val _news = MutableStateFlow<List<News>?>(null)
    val news: StateFlow<List<News>?> = _news
    private val _showProgress = MutableStateFlow<Boolean>(false)
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

private val date1 = Date
    .from(Instant.now().minusSeconds(10))
private val date2 = Date
    .from(Instant.now().minusSeconds(20))
private val date3 = Date
    .from(Instant.now().minusSeconds(30))

private val aName = "Some name"
private val someNews =
    listOf(News(date3), News(date1), News(date2))

class MainViewModelTests {
    private lateinit var scheduler: TestCoroutineScheduler
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        scheduler = TestCoroutineScheduler()
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
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
        scheduler.runCurrent()

        // then
        assertEquals(true, viewModel.showProgress.value)

        // when
        scheduler.advanceUntilIdle()

        // then
        assertEquals(false, viewModel.showProgress.value)
    }

    @Test
    fun `user name is shown`() {
        // when
        scheduler.advanceUntilIdle()

        // then
        assertEquals(aName, viewModel.userName.value)
    }

    @Test
    fun `sorted news are shown`() {
        // when
        scheduler.advanceUntilIdle()

        // then
        val someNewsSorted =
            listOf(News(date1), News(date2), News(date3))
        assertEquals(someNewsSorted, viewModel.news.value)
    }

    @Test
    fun `user and news are called concurrently`() {
        // when
        scheduler.advanceUntilIdle()

        // then
        assertEquals(1000, scheduler.currentTime)
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
