package coroutines.test.testuserdetailsrepository

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
    suspend fun getUserDetails(): UserDetails = coroutineScope {
        val stored = userDatabase.load()
        if (stored != null) {
            return@coroutineScope stored
        }
        val name = async { client.getName() }
        val friends = async { client.getFriends() }
        val profile = async { client.getProfile() }
        val details = UserDetails(
            name = name.await(),
            friends = friends.await(),
            profile = profile.await(),
        )
        backgroundScope.launch { userDatabase.save(details) }
        details
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
                // TODO
                return "Ben"
            }
    
            override suspend fun getFriends(): List<Friend> {
                // TODO
                return listOf(Friend("some-friend-id-1"))
            }
    
            override suspend fun getProfile(): Profile {
                // TODO
                return Profile("Example description")
            }
        }
        var savedDetails: UserDetails? = null
        val database = object : UserDetailsDatabase {
            override suspend fun load(): UserDetails? {
                // TODO
                return savedDetails
            }
    
            override suspend fun save(user: UserDetails) {
                // TODO
                savedDetails = user
            }
        }
    
        val repo: UserDetailsRepository = TODO()
    
        // when
        val details = repo.getUserDetails()
    
        // then data are fetched asynchronously
        // TODO
    
        // when all children are finished
        // TODO
    
        // then data are saved to the database
        // TODO
    
        // when getting details again
        // TODO
    
        // then data are loaded from the database
        // TODO
    }
    
    
}
