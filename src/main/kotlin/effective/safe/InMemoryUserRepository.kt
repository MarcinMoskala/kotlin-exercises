package effective.safe.inmemoryuserrepository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Before
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.typeOf
import kotlin.test.*

class InMemoryUserRepository {
    private val users = mutableSetOf<User>()

    fun addUser(user: User) {
        users.add(user)
    }

    fun getUsers() = users

    fun hasUser(user: User): Boolean = user in users

    fun changeSurname(userId: Int, newSurname: String) {
        users.find { it.id == userId }?.surname = newSurname
    }

    fun changeAllSurnames(newSurname: String) {
        users.forEach { it.surname = newSurname }
    }

    data class User(val id: Int, val name: String, var surname: String)
}

class InMemoryNewsRepositoryTest {

    lateinit var repo: InMemoryUserRepository

    @Before
    fun setup() {
        repo = InMemoryUserRepository()
        List(1000) { InMemoryUserRepository.User(it * 2, "Name$it", "Surname$it") }
            .forEach { repo.addUser(it) }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `getUsers should not expose mutation point`() {
        assertEquals(typeOf<Set<InMemoryUserRepository.User>>(), InMemoryUserRepository::getUsers.returnType)
    }

    @Test
    fun `should allow concurrent user addition`(): Unit = runBlocking(Dispatchers.IO) {
        val newUsers = List(1000) { InMemoryUserRepository.User(it * 2, "NewName$it", "NewSurname$it") }

        coroutineScope {
            for (newUser in newUsers) {
                launch {
                    repo.addUser(newUser)
                }
            }
        }

        assertEquals(2000, repo.getUsers().size)
    }

    @Test
    fun `should allow concurrent username change`(): Unit = runBlocking(Dispatchers.IO) {
        val users = repo.getUsers()

        coroutineScope {
            for (user in users) {
                launch {
                    repo.changeSurname(user.id, "NewSurname")
                }
            }
        }

        assertEquals(1000, repo.getUsers().size)
        assertEquals(List(1000) { "NewSurname" }, repo.getUsers().map { it.surname })
    }

    @Test
    fun `should not have user lost after surname change`() {
        val users = repo.getUsers()
        val newUsers = mutableListOf<InMemoryUserRepository.User>()
        for (user in users) {
            val newSurname = "NewSurname${user.id}"
            repo.changeSurname(user.id, newSurname)
            newUsers.add(user.copy(surname = newSurname))
        }

        for (user in newUsers) {
            assertTrue(repo.hasUser(user))
        }
    }

    @Test
    fun `should allow parallel write and read`(): Unit = runBlocking(Dispatchers.IO) {
        repeat(10) {
            launch {
                repeat(1000) {
                    repo.addUser(InMemoryUserRepository.User(it * 2 + 1, "NewUserName$it", "NewUserSurname$it"))
                }
            }
            launch {
                repeat(1000) {
                    val users = repo.getUsers()
                    // The expected problem here is ConcurrentModificationException from inside count or sumOf
                    assertTrue(users.count() >= 1000, "Problem with $users, size ${users.size}")
                    assertTrue(users.sumOf { it.id } > 500_000)
                }
            }
        }
    }

    @Test
    fun `When we set new surnames, they should all be the same`(): Unit = runBlocking(Dispatchers.IO) {
        repo.changeAllSurnames("NewSurname")
        launch {
            repeat(1000) {
                repo.changeAllSurnames("NewSurname$it")
            }
        }
        launch {
            repeat(1000) {
                assertEquals(1, repo.getUsers().distinctBy { it.surname }.size)
            }
        }
    }
}

class GettingUserTest {

    @Test
    fun `changeUserSurname when user cannot be found, proper error is displayed`() {
        val repo = InMemoryUserRepository()
        val exception = assertThrows(IllegalArgumentException::class.java) { repo.changeSurname(123, "XXX") }
        assertEquals("No such user in the repository", exception.message, "Function has correct message")
    }

    @ExperimentalStdlibApi
    @Test
    fun `getById has correct signature`() {
        val repoClass = InMemoryUserRepository::class
        val method = repoClass.memberFunctions.singleOrNull { it.name == "getById" }
        assertNotNull(method, "Method getById needs to be implemented")

        // Check return type
        assertFalse(method.returnType.isMarkedNullable, "Return type should not be nullable")
        assertEquals(typeOf<InMemoryUserRepository.User>(), method.returnType, "Function should return User")
        // Check parameter
        assertTrue(method.parameters.size == 2, "There is only a single expected argument (+ dispatch receiver)")
        // The first parameter is dispatch receiver - reference to the class
        assertEquals(typeOf<InMemoryUserRepository>(), method.parameters[0].type, "Parameter type should be Int")
        assertEquals(typeOf<Int>(), method.parameters[1].type, "Parameter type should be Int")
        assertTrue(method.typeParameters.isEmpty(), "There should be no type parameters")
    }

    @Test
    fun `getById works correctly`() {
        val repo = InMemoryUserRepository()
        val repoClass = repo::class
        val method = repoClass.memberFunctions.singleOrNull { it.name == "getById" }
        assertNotNull(method, "Method getById needs to be implemented")

        // Works for correct user
        val user = InMemoryUserRepository.User(10, "A", "B")
        repo.addUser(user)
        assertEquals(user, method.call(repo, user.id))

        // Throws a correct error for lack of user with given id
        val exception =
            assertThrows(InvocationTargetException::class.java) { // All errors should be wrapped into this type by reflection
                method.call(repo, 0)
            }.targetException // We unpack to get actual exception throw by this function
        assertTrue(exception is NoSuchElementException, "The type of error should be NoSuchElementException")
        assertEquals("No user with id 0", exception.message, "Check for concrete error message")
    }

    @ExperimentalStdlibApi
    @Test
    fun `getByIdOrNull has correct signature`() {
        val repoClass = InMemoryUserRepository::class
        val method = repoClass.memberFunctions.singleOrNull { it.name == "getByIdOrNull" }
        assertNotNull(method, "Method getByIdOrNull needs to be implemented")

        // Check return type
        assertTrue(method.returnType.isMarkedNullable, "Return type should be nullable")
        assertEquals(typeOf<InMemoryUserRepository.User?>(), method.returnType, "Function should return User")
        // Check parameter
        assertTrue(method.parameters.size == 2, "There is only a single expected argument (+ dispatch receiver)")
        // The first parameter is dispatch receiver - reference to the class
        assertEquals(typeOf<InMemoryUserRepository>(), method.parameters[0].type, "Parameter type should be Int")
        assertEquals(typeOf<Int>(), method.parameters[1].type, "Parameter type should be Int")
        assertTrue(method.typeParameters.isEmpty(), "There should be no type parameters")
    }

    @Test
    fun `getByIdOrNull works correctly`() {
        val repo = InMemoryUserRepository()
        val repoClass = repo::class
        val method = repoClass.memberFunctions.singleOrNull { it.name == "getByIdOrNull" }
        assertNotNull(method, "Method getByIdOrNull needs to be implemented")

        // Works for correct user
        val user = InMemoryUserRepository.User(10, "A", "B")
        repo.addUser(user)
        assertEquals(user, method.call(repo, user.id))

        // Returns a null lack of user with given id
        val result = method.call(repo, 0)
        assertNull(result, "Function should return null when no user with given id")
    }

    @ExperimentalStdlibApi
    @Test
    fun `getByIdOrDefault has correct signature`() {
        val repoClass = InMemoryUserRepository::class
        val method = repoClass.memberFunctions.singleOrNull { it.name == "getByIdOrDefault" }
        assertNotNull(method, "Method getByIdOrDefault needs to be implemented")

        // Check return type
        assertFalse(method.returnType.isMarkedNullable, "Return type should not be nullable")
        assertEquals(typeOf<InMemoryUserRepository.User>(), method.returnType, "Function should return User")
        // Check parameter
        assertTrue(method.parameters.size == 3, "There are two parameters in this function (+ dispatch receiver)")
        val (dispatchReceiver, param1, param2) = method.parameters
        assertEquals(typeOf<InMemoryUserRepository>(), dispatchReceiver.type)
        assertEquals(typeOf<Int>(), param1.type, "The type of the first parameter should be Int")
        assertEquals(
            typeOf<InMemoryUserRepository.User>(),
            param2.type,
            "The type of the second parameter should be User"
        )
        assertTrue(method.typeParameters.isEmpty(), "There should be no type parameters")
    }

    @Test
    fun `getByIdOrDefault works correctly`() {
        val repo = InMemoryUserRepository()
        val repoClass = repo::class
        val method = repoClass.memberFunctions.singleOrNull { it.name == "getByIdOrDefault" }
        assertNotNull(method, "Method getByIdOrDefault needs to be implemented")
        val default = InMemoryUserRepository.User(100, "C", "D")

        // Works for correct user
        val user = InMemoryUserRepository.User(10, "A", "B")
        repo.addUser(user)
        assertEquals(user, method.call(repo, user.id, default))

        // Returns a null lack of user with given id
        val result = method.call(repo, 0, default)
        assertEquals(default, result, "Function should return null when no user with given id")
    }
}
