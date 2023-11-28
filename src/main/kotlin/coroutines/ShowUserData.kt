package userdata

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

suspend fun showUserData(repo: UserDataRepository, view: UserDataView) {
    // TODO
}

fun main() = runBlocking {
    showUserData(TestUserDataRepository(), TestUserDataView())
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
