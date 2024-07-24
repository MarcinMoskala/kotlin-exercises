package coroutines.starting.userdetailsrepository

import kotlinx.coroutines.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class UserDetailsRepository(
    private val client: UserDataClient,
    private val userDatabase: UserDetailsDatabase,
    private val backgroundScope: CoroutineScope,
) {
    suspend fun getUserDetails(): UserDetails {
        TODO()
    }
}

interface UserDataClient {
    suspend fun getName(): String
    suspend fun getFriends(): List<Friend>
    suspend fun getProfile(): Profile
}

interface UserDetailsDatabase {
    suspend fun load(): UserDetails?
    suspend fun save(user: UserDetails)
}

data class UserDetails(
    val name: String,
    val friends: List<Friend>,
    val profile: Profile
)

data class Friend(val id: String)
data class Profile(val description: String)

@Suppress("FunctionName")
class UserDetailsRepositoryTest {

    @Test
    fun `should fetch details asynchronously`() = runTest {
        // given
        val client = object : UserDataClient {
            override suspend fun getName(): String {
                delay(100)
                return "Ben"
            }

            override suspend fun getFriends(): List<Friend> {
                delay(200)
                return listOf(Friend("friend-id-1"))
            }

            override suspend fun getProfile(): Profile {
                delay(300)
                return Profile("Example description")
            }
        }
        val database = InMemoryDatabase()
        val repo = UserDetailsRepository(client, database, backgroundScope)

        // when
        val details = repo.getUserDetails()

        // then
        assertEquals("Ben", details.name)
        assertEquals("friend-id-1", details.friends.single().id)
        assertEquals("Example description", details.profile.description)
        assertEquals(300, currentTime)
    }

    @Test
    fun `should save details to database asynchronously`() = runTest {
        // given
        val client = object : UserDataClient {
            override suspend fun getName(): String {
                delay(100)
                return "Ben"
            }

            override suspend fun getFriends(): List<Friend> {
                delay(100)
                return listOf(Friend("friend-id-1"))
            }

            override suspend fun getProfile(): Profile {
                delay(100)
                return Profile("Example description")
            }
        }
        val database = InMemoryDatabase(saveTime = 1_000)
        val repo = UserDetailsRepository(client, database, backgroundScope)

        // when
        repo.getUserDetails()

        // then
        assertEquals(100, currentTime)

        // when
        backgroundScope.coroutineContext.job.children
            .forEach { it.join() }

        // then
        assertEquals(1_100, currentTime)
    }

    @Test
    fun `should load from database`() = runTest {
        // given
        val database = InMemoryDatabase(loadTime = 10)
        database.save(UserDetails("Ben", listOf(Friend("friend-id-1")), Profile("Example description")))
        val client = object : UserDataClient {
            override suspend fun getName(): String {
                error("Should not be called")
            }

            override suspend fun getFriends(): List<Friend> {
                error("Should not be called")
            }

            override suspend fun getProfile(): Profile {
                error("Should not be called")
            }
        }
        val repo = UserDetailsRepository(client, database, backgroundScope)

        // when
        val details = repo.getUserDetails()

        // then
        assertEquals("Ben", details.name)
        assertEquals("friend-id-1", details.friends.single().id)
        assertEquals("Example description", details.profile.description)
        assertEquals(10, currentTime)
    }

    class InMemoryDatabase(
        private val saveTime: Long = 0,
        private val loadTime: Long = 0,
    ) : UserDetailsDatabase {
        private var stored: UserDetails? = null
        override suspend fun load(): UserDetails? {
            delay(loadTime)
            return stored
        }
        override suspend fun save(user: UserDetails) {
            delay(saveTime)
            stored = user
        }
    }
}
