package functional.scope.orthrow

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

// TODO

@Suppress("RedundantNullableReturnType")
class OrThrowTest {
//    @Test
//    fun `should throw for null value`() {
//        val value: String? = null
//        val exception = RuntimeException("Value is null")
//        val result = runCatching { value.orThrow { exception } }
//        assertEquals(exception, result.exceptionOrNull())
//    }
//
//    @Test
//    fun `should return value for non-null value`() {
//        val value: String? = "Hello"
//        val result = value.orThrow { RuntimeException("Value is null") }
//        assertEquals("Hello", result)
//    }
//
//    private val value: String? = "Hello"
//    val result = value.orThrow { RuntimeException("Value is null") }
//
//    @Test
//    fun `should specify result type as non-nullable`() {
//        assertFalse(::result.returnType.isMarkedNullable)
//    }
}

//class UserController(
//    private val userRepository: UserRepository,
//    private val logger: Logger,
//) {
//    fun getUser(userId: String) = userRepository
//        .getUser(userId)
//        .orThrow { UserNotFoundException(userId) }
//        .also { logger.log("User with id ${it.id} found") }
//        .toUserJson()
//}
//
//interface UserRepository {
//    fun getUser(userId: String): User?
//}
//
//interface Logger {
//    fun log(message: String)
//}
//
//data class User(
//    val id: String,
//    val name: String,
//)
//
//data class UserJson(
//    val id: String,
//    val name: String,
//)
//
//fun User.toUserJson() = UserJson(
//    id = id,
//    name = name
//)
//
//class UserNotFoundException(userId: String) : Throwable("User $userId not found")
//
//class FakeLogger: Logger {
//    var messages: List<String> = emptyList()
//
//    override fun log(message: String) {
//        this.messages += message
//    }
//}
//
//class FakeUserRepository() : UserRepository {
//    var users: List<User> = emptyList()
//
//    fun addUser(user: User) {
//        users += user
//    }
//
//    override fun getUser(userId: String): User? = users
//        .find { it.id == userId }
//}
//
//class UserControllerTest {
//    private val logger = FakeLogger()
//    private val userRepository = FakeUserRepository()
//    private val userController = UserController(userRepository, logger)
//
//    @Before
//    fun cleanup() {
//        logger.messages = emptyList()
//        userRepository.users = emptyList()
//    }
//
//    @Test(expected = UserNotFoundException::class)
//    fun `should throw for non-existing user`() {
//        userController.getUser("1")
//    }
//
//    @Test
//    fun `should return user dto for existing user`() {
//        userRepository.addUser(User("1", "John"))
//        val result = userController.getUser("1")
//        assertEquals(UserJson("1", "John"), result)
//    }
//
//    @Test
//    fun `should log for existing user`() {
//        userRepository.addUser(User("1", "John"))
//        userController.getUser("1")
//        assertEquals(listOf("User with id 1 found"), logger.messages)
//    }
//}
