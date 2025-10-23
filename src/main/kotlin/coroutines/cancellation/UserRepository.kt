package coroutines.cancellation.userrepository

import kotlinx.coroutines.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class UserRepository(
    private val storage: FileStorage,
    private val database: UserDatabaseDao,
    private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun updateUser() {
        val user = storage.readUser() // blocking
        val userSettings = storage.readUserSettings(user.id) // blocking

        try {
            database.updateUserInDatabase(user, userSettings) // suspending
        } catch (e: CancellationException) {
            database.revertUnfinishedTransactions() // suspending
        }
    }
}

interface FileStorage {
    fun readUser(): User
    fun readUserSettings(userId: String): UserSettings
}

interface UserDatabaseDao {
    suspend fun updateUserInDatabase(user: User, userSettings: UserSettings)
    suspend fun revertUnfinishedTransactions()
}

data class User(val id: String, val name: String)
data class UserSettings(val userId: String, val language: String)

class CancellationExerciseTest {
    @Test
    fun `should update user in database`() = runTest {
        // given
        var updatedUser: User? = null
        var updatedUserSettings: UserSettings? = null
        val storage = object : FileStorage {
            override fun readUser(): User {
                return User("1", "John Doe")
            }

            override fun readUserSettings(userId: String): UserSettings {
                return UserSettings(userId, "EN")
            }
        }
        val database = object : UserDatabaseDao {
            override suspend fun updateUserInDatabase(user: User, userSettings: UserSettings) {
                updatedUser = user
                updatedUserSettings = userSettings
            }

            override suspend fun revertUnfinishedTransactions() {
                updatedUser = null
                updatedUserSettings = null
            }
        }
        val dispatcher = coroutineContext[CoroutineDispatcher]!!
        val userRepository = UserRepository(storage, database, dispatcher)

        // when
        userRepository.updateUser()

        // then
        assertEquals(User("1", "John Doe"), updatedUser)
        assertEquals(UserSettings("1", "EN"), updatedUserSettings)
    }

    @Test
    fun `should not block the caller thread`() = runTest {
        // given
        var blockedThreadsNames = listOf<String>()
        val storage = object : FileStorage {
            override fun readUser(): User {
                blockedThreadsNames += Thread.currentThread().name
                return User("1", "John Doe")
            }

            override fun readUserSettings(userId: String): UserSettings {
                blockedThreadsNames += Thread.currentThread().name
                return UserSettings(userId, "EN")
            }
        }
        val database = object : UserDatabaseDao {
            override suspend fun updateUserInDatabase(user: User, userSettings: UserSettings) {
                delay(1000)
            }

            override suspend fun revertUnfinishedTransactions() {
                delay(100)
            }
        }
        val userRepository = UserRepository(storage, database, Dispatchers.IO)
        val callerThreadName = "test"

        // when
        withContext(newSingleThreadContext(callerThreadName)) {
            userRepository.updateUser()
        }

        // then
        assert(blockedThreadsNames.size == 2)
        assert(callerThreadName !in blockedThreadsNames)
    }

    @Test
    fun `should allow cancellation between two blocking functions`() = runTest {
        // given
        var job: Job? = null
        var readUserSettingsInvoked = false
        val storage = object : FileStorage {
            override fun readUser(): User {
                job?.cancel()
                return User("1", "John Doe")
            }

            override fun readUserSettings(userId: String): UserSettings {
                readUserSettingsInvoked = true
                return UserSettings(userId, "EN")
            }
        }
        val database = object : UserDatabaseDao {
            override suspend fun updateUserInDatabase(user: User, userSettings: UserSettings) {
                delay(1000)
            }

            override suspend fun revertUnfinishedTransactions() {
                delay(100)
            }
        }
        val dispatcher = coroutineContext[CoroutineDispatcher]!!
        val userRepository = UserRepository(storage, database, dispatcher)

        // when
        job = launch {
            userRepository.updateUser()
        }
        job.join()

        // then
        assert(job.isCancelled)
        assert(!readUserSettingsInvoked)
        assert(currentTime == 0L)
    }

    @Test
    fun `should invoke revertUnfinishedTransactions on cancellation during suspending function`() = runTest {
        // given
        var revertUnfinishedTransactionsInvoked = false
        val storage = object : FileStorage {
            override fun readUser(): User {
                return User("1", "John Doe")
            }

            override fun readUserSettings(userId: String): UserSettings {
                return UserSettings(userId, "EN")
            }
        }
        val database = object : UserDatabaseDao {
            override suspend fun updateUserInDatabase(user: User, userSettings: UserSettings) {
                delay(1000)
            }

            override suspend fun revertUnfinishedTransactions() {
                revertUnfinishedTransactionsInvoked = true
                delay(100)
            }
        }
        val dispatcher = coroutineContext[CoroutineDispatcher]!!
        val userRepository = UserRepository(storage, database, dispatcher)

        // when
        val job = launch {
            userRepository.updateUser()
        }
        delay(100) // ensure we are in the suspending function
        job.cancel()
        job.join()

        // then
        assert(job.isCancelled)
        assert(revertUnfinishedTransactionsInvoked)
        assert(currentTime == 200L) // the moment when we canceled + execution time of revertUnfinishedTransactions
    }

    @Test
    fun `should not consume CancellationException`() = runTest {
        // given
        val storage = object : FileStorage {
            override fun readUser(): User {
                return User("1", "John Doe")
            }

            override fun readUserSettings(userId: String): UserSettings {
                return UserSettings(userId, "EN")
            }
        }
        val database = object : UserDatabaseDao {
            override suspend fun updateUserInDatabase(user: User, userSettings: UserSettings) {
                delay(1000)
            }

            override suspend fun revertUnfinishedTransactions() {
                delay(100)
            }
        }
        val dispatcher = coroutineContext[CoroutineDispatcher]!!
        val userRepository = UserRepository(storage, database, dispatcher)

        // when
        var result: Result<Unit>? = null
        val job = launch {
            result = runCatching {
                userRepository.updateUser()
            }
        }
        delay(500)
        job.cancelAndJoin()

        // then
        assert(result?.exceptionOrNull() is CancellationException)
    }
}
