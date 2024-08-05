package examples.coroutines.testing

import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*

//fun main() {
//   val scheduler = TestCoroutineScheduler()
//
//   println(scheduler.currentTime) // 0
//   scheduler.advanceTimeBy(1_000)
//   println(scheduler.currentTime) // 1000
//   scheduler.advanceTimeBy(1_000)
//   println(scheduler.currentTime) // 2000
//}

//fun main() {
//   val scheduler = TestCoroutineScheduler()
//   val testDispatcher = StandardTestDispatcher(scheduler)
//
//   CoroutineScope(testDispatcher).launch {
//       println("Some work 1")
//       delay(1000)
//       println("Some work 2")
//       delay(1000)
//       println("Coroutine done")
//   }
//
//   println("[${scheduler.currentTime}] Before")
//   scheduler.advanceTimeBy(1_000)
//   println("[${scheduler.currentTime}] After")
//}

//fun main() = runTest {
//   println(currentTime)
//   coroutineScope {
//       launch { delay(1000) }
//       launch { delay(1500) }
//       launch { delay(2000) }
//   }
//    println(currentTime)
//}

class TestBackgroundScope {
    
    @Test
    fun `should increment counter`() = runTest {
        var i = 0
        launch {
            while (true) {
                delay(1000)
                i++
            }
        }
        
        delay(1001)
        assertEquals(1, i)
        delay(1000)
        assertEquals(2, i)
        
        // Test would pass if we added
        // coroutineContext.job.cancelChildren()
    }
}

//fun main() {
//    CoroutineScope(StandardTestDispatcher()).launch {
//        print("A")
//        delay(1)
//        print("B")
//    }
//    CoroutineScope(UnconfinedTestDispatcher()).launch {
//        print("C")
//        delay(1)
//        print("D")
//    }
//}

class CurrentUserFactory(private val repo: UserRepository) {
    suspend fun produceCurrentUserSync(): User {
       val profile = repo.getProfile()
       val friends = repo.getFriends()
       return User(profile, friends)
    }
    
    suspend fun produceCurrentUserAsync(): User = coroutineScope {
       val profile = async { repo.getProfile() }
       val friends = async { repo.getFriends() }
       User(profile.await(), friends.await())
    }
}

data class User(val profile: Profile, val friends: List<Profile>)
data class Profile(val name: String)
interface UserRepository {
    suspend fun getProfile(): Profile
    suspend fun getFriends(): List<Profile>
}

class CurrentUserFactoryTest {
    @Test
    fun `should produce current user synchronously`() = runTest {
        val repo = DelayedUserRepository()
        val factory = CurrentUserFactory(repo)
        
        val user = factory.produceCurrentUserSync()
        
        assertEquals(User(Profile("John"), listOf(Profile("Alice"), Profile("Bob"))), user)
        assertEquals(2000, currentTime)
    }
    
    @Test
    fun `should produce current user asynchronously`() = runTest {
        val repo = DelayedUserRepository()
        val factory = CurrentUserFactory(repo)
        
        val user = factory.produceCurrentUserAsync()
        
        assertEquals(User(Profile("John"), listOf(Profile("Alice"), Profile("Bob"))), user)
        assertEquals(1000, currentTime)
    }
    
    class DelayedUserRepository : UserRepository {
        override suspend fun getProfile(): Profile {
            delay(1000)
            return Profile("John")
        }
        
        override suspend fun getFriends(): List<Profile> {
            delay(1000)
            return listOf(Profile("Alice"), Profile("Bob"))
        }
    }
}
