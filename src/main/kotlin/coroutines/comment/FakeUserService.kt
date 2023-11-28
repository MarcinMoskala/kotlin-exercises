package comment

import domain.comment.NoSuchUserException
import domain.comment.User
import domain.comment.UserService
import kotlinx.coroutines.delay

class FakeUserService : UserService {
    var findUserDelay: Long? = null
    private var users = listOf<User>()
    private var tokens = mapOf<String, String>()

    fun hasUsers(vararg user: User) {
        users = users + user
    }

    fun hasToken(token: String, userId: String) {
        tokens = tokens + (token to userId)
    }

    fun clear() {
        users = emptyList()
        tokens = mapOf()
        findUserDelay = null
    }

    override fun readUserId(token: String): String =
        tokens[token] ?: throw NoSuchUserException

    override suspend fun findUser(token: String): User {
        findUserDelay?.let { delay(it) }
        return findUserById(readUserId(token))
    }

    override suspend fun findUserById(id: String): User {
        findUserDelay?.let { delay(it) }
        return users.find { it.id == id } ?: throw NoSuchUserException
    }
}