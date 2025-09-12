package examples.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class FetchUserUseCase(
    val userRepository: UserRepository
) {
    suspend fun fetchUser(): UserData = coroutineScope {
        val userDetails = async { userRepository.fetchUserDetails() }
        val posts = async { userRepository.fetchPosts() }
        UserData(userDetails.await(), posts.await())
    }
}

interface UserRepository {
    suspend fun fetchUserDetails(): UserDetails
    suspend fun fetchPosts(): List<Post>
}

data class UserData(
    val userDetails: UserDetails,
    val posts: List<Post>
)

data class UserDetails(val id: String, val name: String)
data class Post(val id: String, val content: String)

class FetchUserTest {
    @Test
    fun `should fetch user data and posts asynchronously`() = runTest {
        // given
        val repository = object : UserRepository {
            override suspend fun fetchUserDetails(): UserDetails {
                delay(1000)
                return UserDetails("1", "John Doe")
            }

            override suspend fun fetchPosts(): List<Post> {
                delay(1000)
                return listOf(Post("101", "Hello World"), Post("102", "Kotlin Coroutines"))
            }
        }
        val useCase = FetchUserUseCase(repository)

        // when
        val userData = useCase.fetchUser()

        // then
        assert(userData.userDetails.name == "John Doe")
        assert(userData.posts.size == 2)
        assert(currentTime == 1000L)
    }

    @Test
    fun `should cancel children on caller cancellation`() = runTest {
        // given
        var children1Job: Job? = null
        var children2Job: Job? = null
        val repository = object : UserRepository {
            override suspend fun fetchUserDetails(): UserDetails {
                children1Job = currentCoroutineContext()[Job]
                delay(1000)
                return UserDetails("1", "John Doe")
            }

            override suspend fun fetchPosts(): List<Post> {
                children2Job = currentCoroutineContext()[Job]
                delay(1000)
                return listOf(Post("101", "Hello World"), Post("102", "Kotlin Coroutines"))
            }
        }
        val useCase = FetchUserUseCase(repository)

        // when
        val job = launch {
            useCase.fetchUser()
        }
        delay(500)
        job.cancelAndJoin()

        // then
        assert(job.isCancelled) // The parent job should be cancelled
        assert(children1Job?.isCancelled == true) // Both children should be cancelled
        assert(children2Job?.isCancelled == true)
        assert(currentTime == 500L) // Immediately after cancellation
    }

    @Test
    fun `should handle exception in one of the children`() = runTest {
        // given
        class MyException(message: String) : Exception(message)
        var children1Job: Job? = null
        val repository = object : UserRepository {
            override suspend fun fetchUserDetails(): UserDetails {
                children1Job = currentCoroutineContext()[Job]
                delay(1000)
                return UserDetails("1", "John Doe")
            }

            override suspend fun fetchPosts(): List<Post> {
                delay(500)
                throw MyException("Failed to fetch posts")
            }
        }
        val useCase = FetchUserUseCase(repository)

        // when
        val result = runCatching { useCase.fetchUser() }

        // then
        assert(result.exceptionOrNull() is MyException) // The same exception should be propagated
        assert(currentTime == 500L) // Immediately after the exception
        assert(children1Job?.isCancelled == true) // The other child should be cancelled
    }
}