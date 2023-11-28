@file:OptIn(ExperimentalCoroutinesApi::class)

package examples

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
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

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName
    private val _news = MutableLiveData<List<News>>()
    val news: LiveData<List<News>> = _news

    fun onCreate() {
        scope.launch {
            val user = userRepo.getUser()
            _userName.value = user.name
        }
        scope.launch {
            _news.value = newsRepo.getNews()
                .sortedByDescending { it.date }
        }
    }
}

abstract class BaseViewModel : ViewModel() {
    private val context = Dispatchers.Main + SupervisorJob()
    val scope = CoroutineScope(context)

    fun onDestroy() {
        context.cancelChildren()
    }
}

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

private val date1 = Date.from(Instant.now().minusSeconds(10))
private val date2 = Date.from(Instant.now().minusSeconds(20))
private val date3 = Date.from(Instant.now().minusSeconds(30))

val aName = "Some name"
val someNews = listOf(News(date3), News(date1), News(date2))
val viewModel = MainViewModel(
    userRepo = FakeUserRepository(aName),
    newsRepo = FakeNewsRepository(someNews)
)

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

class MainViewModelTests {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `user name is shown`() {
        // when
        viewModel.onCreate()
        mainCoroutineRule.scheduler.advanceUntilIdle()

        // then
        assertEquals(aName, viewModel.userName.value)
    }

    @Test
    fun `sorted news are shown`() {
        // when
        viewModel.onCreate()
        mainCoroutineRule.scheduler.advanceUntilIdle()

        // then
        val someNewsSorted =
            listOf(News(date1), News(date2), News(date3))
        assertEquals(someNewsSorted, viewModel.news.value)
    }

    @Test
    fun `user and news are called concurrently`() {
        // when
        viewModel.onCreate()
        mainCoroutineRule.scheduler.advanceUntilIdle()


        // then
        assertEquals(1000, mainCoroutineRule.scheduler.currentTime)
    }
}