package userdata

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ObsoleteCoroutinesApi
@Suppress("FunctionName")
class ShowUserDataTest {

    @Test
    fun `should show data on view`() = runBlocking {
        // given
        val repo = FakeUserDataRepository()
        val view = FakeUserDataView()

        // when
        showUserData(repo, view)

        // then
        assertEquals(
            listOf(User("Ben", listOf(Friend("some-friend-id-1")), Profile("Example description"))),
            view.printed
        )
    }

    @Test(timeout = 800)
    fun `should load user data asynchronously and not wait for notify`() = runBlocking {
        // given
        val repo = FakeUserDataRepository()
        val view = FakeUserDataView()

        // when
        showUserData(repo, view)

        // then
        assertEquals(1, view.printed.size)
    }

    @Test
    fun `should start notify profile shown`() = runBlocking {
        // given
        val repo = FakeUserDataRepository()
        val view = FakeUserDataView()

        // when
        showUserData(repo, view)
        delay(100)

        // then
        assertTrue(repo.notifyCalled)
    }

    class FakeUserDataRepository : UserDataRepository {
        var notifyCalled = false

        override suspend fun notifyProfileShown() {
            notifyCalled = true
            delay(2000)
        }

        override suspend fun getName(): String {
            delay(300)
            return "Ben"
        }

        override suspend fun getFriends(): List<Friend> {
            delay(300)
            return listOf(Friend("some-friend-id-1"))
        }

        override suspend fun getProfile(): Profile {
            delay(300)
            return Profile("Example description")
        }
    }

    class FakeUserDataView : UserDataView {
        var printed = listOf<User>()

        override fun show(user: User) {
            printed = printed + user
        }
    }
}
