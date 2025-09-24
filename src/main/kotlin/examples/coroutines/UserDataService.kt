package examples.coroutines.UserDataService

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserDataService(
    private val userRemoteRepository: UserRemoteRepository,
    private val userLocalRepository: UserLocalRepository,
    private val backgroundScope: CoroutineScope,
) {
    private var userData: UserData? = null

    suspend fun getUserData(): UserData {
        userData?.let { return it }
        userData = userLocalRepository.getUserDataOrNull()
        userData?.let { return it }
        val newUserData = userRemoteRepository.getUserData()
        userData = newUserData
        backgroundScope.launch {
            userLocalRepository.saveUserData(newUserData)
        }
        return newUserData
    }
}

interface UserRemoteRepository {
    suspend fun getUserData(): UserData
}
interface UserLocalRepository {
    suspend fun getUserDataOrNull(): UserData?
    suspend fun saveUserData(userData: UserData)
}

data class UserData(val name: String)

class UserDataServiceTest {
    private val userRemoteRepository = FakeUserRemoteRepository()
    private val userLocalRepository = FakeUserLocalRepository()
    private val testScope = TestScope()

    private val service = UserDataService(
        userRemoteRepository = userRemoteRepository,
        userLocalRepository = userLocalRepository,
        backgroundScope = testScope
    )

    @Test
    fun `should asynchronously store fetched user data`() = testScope.runTest {
        // given
        val expectedUserData = UserData("John Doe")
        userRemoteRepository.setUserData(expectedUserData)
        userLocalRepository.setSaveTime(1000)

        // when
        val result = service.getUserData()

        // then
        assertEquals(expectedUserData, result)
        assertEquals(null, userLocalRepository.getUserDataOrNull())
        assertEquals(0, currentTime)

        // when
        advanceUntilIdle()

        // then
        assertEquals(1000, currentTime)
        assertEquals(expectedUserData, userLocalRepository.getUserDataOrNull())
    }
}

class FakeUserRemoteRepository : UserRemoteRepository {
    private var userData: UserData = UserData("")

    fun setUserData(userData: UserData) {
        this.userData = userData
    }

    override suspend fun getUserData(): UserData {
        return userData
    }
}

class FakeUserLocalRepository : UserLocalRepository {
    private var userData: UserData? = null
    private var saveTime: Long = 0

    fun setSaveTime(time: Long) {
        this.saveTime = time
    }

    override suspend fun getUserDataOrNull(): UserData? {
        return userData
    }

    override suspend fun saveUserData(userData: UserData) {
        kotlinx.coroutines.delay(saveTime)
        this.userData = userData
    }
}