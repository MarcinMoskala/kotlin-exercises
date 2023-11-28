package backend

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals

@Suppress("FunctionName")
class ApiTests {

    @After
    fun clean() {
        cleanupApi()
    }

    class FakeEmailService : EmailService {
        var emailsSend = listOf<Pair<String, String>>()

        override suspend fun sendEmail(to: String, body: String) {
            emailsSend = emailsSend + (to to body)
        }
    }

    class FakeDatabase : Database {
        var users = listOf<User>()

        override suspend fun getUsers(): List<User> {
            return users
        }

        override suspend fun addUser(user: User) {
            users = users + user
        }
    }

    @Test
    fun `When user is added, email is sent`() = runBlocking {
        val emailService = FakeEmailService()
        setupApi(FakeDatabase(), emailService)
        post("user", User("Piotr"))
        assertEquals(listOf("contact@kt.academy" to "New user Piotr"), emailService.emailsSend)
    }

    @Test
    fun `When we add email, it is added to the database`() = runBlocking {
        val database = FakeDatabase()
        setupApi(database, FakeEmailService())

        assertEquals(listOf<User>(), get("users"))
        assertEquals(0, get("users/count"))

        post("user", User("Piotr"))
        assertEquals(listOf(User("Piotr")), database.users)
        val users = get("users")
        assertEquals(listOf(User("Piotr")), users)
        assertEquals(1, get("users/count"))
    }
}