package coroutines.starting

import kotlinx.coroutines.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShowUserDataUseCase(
    private val repo: UserDataRepository,
    private val view: UserDataView,
    private val notificationScope: CoroutineScope,
) {
    suspend fun show() {}
}

interface UserDataRepository {
    suspend fun notifyProfileShown()
    suspend fun getName(): String
    suspend fun getFriends(): List<Friend>
    suspend fun getProfile(): Profile
}

interface UserDataView {
    fun show(user: User)
}

data class User(val name: String, val friends: List<Friend>, val profile: Profile)
data class Friend(val id: String)
data class Profile(val description: String)

class TestUserDataRepository : UserDataRepository {
    override suspend fun notifyProfileShown() {
        delay(10000)
    }

    override suspend fun getName(): String {
        delay(1000)
        return "Ben"
    }

    override suspend fun getFriends(): List<Friend> {
        delay(1000)
        return listOf(Friend("some-friend-id-1"))
    }

    override suspend fun getProfile(): Profile {
        delay(1000)
        return Profile("Example description")
    }
}

class TestUserDataView : UserDataView {
    override fun show(user: User) {
        print(user)
    }
}

@Suppress("FunctionName")
class ShowNewsTest {

    @Test
    fun `should show data on view`() = runTest {
        // given
        val repo = FakeUserDataRepository()
        val view = FakeUserDataView()
        val useCase = ShowUserDataUseCase(repo, view, backgroundScope)

        // when
        useCase.show()

        // then
        assertEquals(
            listOf(User("Ben", listOf(Friend("some-friend-id-1")), Profile("Example description"))),
            view.printed
        )
    }

    @Test
    fun `should load user data asynchronously and not wait for notify`() = runTest {
        // given
        val repo = FakeUserDataRepository()
        val view = FakeUserDataView()
        val useCase = ShowUserDataUseCase(repo, view, backgroundScope)

        // when
        useCase.show()

        // then
        assertEquals(1, view.printed.size)
        assertEquals(FETCH_TIMEOUT, currentTime)
    }

    @Test
    fun `should start notify profile shown after `() = runTest {
        // given
        val repo = FakeUserDataRepository()
        val view = FakeUserDataView()
        val notificationScope = backgroundScope
        val useCase = ShowUserDataUseCase(repo, view, notificationScope)

        // when
        useCase.show()

        // then
        notificationScope.coroutineContext.job.children.forEach { it.join() }
        assertEquals(FETCH_TIMEOUT + NOTIFY_TIMEOUT, currentTime)
        assertTrue(repo.notifyCalled)
    }

    class FakeUserDataRepository : UserDataRepository {
        var notifyCalled = false

        override suspend fun notifyProfileShown() {
            delay(NOTIFY_TIMEOUT)
            notifyCalled = true
        }

        override suspend fun getName(): String {
            delay(FETCH_TIMEOUT)
            return "Ben"
        }

        override suspend fun getFriends(): List<Friend> {
            delay(FETCH_TIMEOUT)
            return listOf(Friend("some-friend-id-1"))
        }

        override suspend fun getProfile(): Profile {
            delay(FETCH_TIMEOUT)
            return Profile("Example description")
        }
    }

    class FakeUserDataView : UserDataView {
        var printed = listOf<User>()

        override fun show(user: User) {
            printed = printed + user
        }
    }
    
    companion object {
        const val FETCH_TIMEOUT = 1234L
        const val NOTIFY_TIMEOUT = 2345L
    }
}
