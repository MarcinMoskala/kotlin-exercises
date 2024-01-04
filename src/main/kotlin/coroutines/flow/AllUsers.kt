package coroutines.flow.allusers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class AllUsers(private val repository: UserRepository) {
    fun getAllUsers(): Flow<User> = TODO()
}

interface UserRepository {
    fun fetchUsers(pageNumber: Int): List<User>
}

data class User(val name: String)

internal class AllUsersTests {

    @Test
    fun test() = runTest {
        val size = 10_000
        val pageSize = 10
        val repo = object : UserRepository {
            val users = List(size) { User("User$it") }
            var timesUsed = 0

            override fun fetchUsers(pageNumber: Int): List<User> =
                users.drop(pageSize * pageNumber)
                    .take(pageSize)
                    .also { timesUsed++ }
        }
        val service = AllUsers(repo)
        val s = service.getAllUsers()
        assertEquals(size, s.count())
        assertEquals(size / pageSize + 1, repo.timesUsed)
    }
}
