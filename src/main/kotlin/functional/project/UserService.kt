package functional.project

import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class UserService(
    private val userRepository: UserRepository,
    private val userDtoFactory: UserDtoFactory,
    private val tokenRepository: TokenRepository,
    private val logger: Logger,
) {
    // Cache definition, that should set clearAfterWrite and clearAfterRead both to 1 minute,
    // and load function to load user by id from userRepository,
    // also loaded user should be stored in userByKeyCache
    private val userByIdCache: Cache<String, User> = cache {
        clearAfterWrite = 1.minutes
        clearAfterRead = 1.minutes
        load { id: String ->
            userRepository.getUser(id)
                ?.toUser()
                ?.also { userByKeyCache.store(it.key, it) }
        }
    }
    // Cache definition, that should set clearAfterWrite and clearAfterRead both to 1 minute,
    // and load function to load user by key from userRepository,
    // also loaded user should be stored in userByIdCache
    private val userByKeyCache: Cache<String, User> = cache {
        clearAfterWrite = 1.minutes
        clearAfterRead = 1.minutes
        load { key: String ->
            userRepository.getUserByKey(key)
                ?.toUser()
                ?.also { userByIdCache.store(it.id, it) }
        }
    }

    // Should get user from cache
    fun getUser(id: String): User? = userByIdCache.get(id)

    // Should get user from cache
    fun getUserByKey(key: String): User? = userByKeyCache.get(key)

    // Should load user from repository, and if password matches, 
    // create, token and return it,
    // otherwise throw error with message "Wrong email or password"
    fun getToken(email: String, passwordHash: String): String =
        userRepository.getUserByEmail(email)
            ?.takeIf { it.passwordHash == passwordHash }
            ?.let { tokenRepository.createToken(it.id, it.isAdmin) }
            ?: error("Wrong email or password")

    // Should update user in repository, and clear cache
    // should log "User updated: $user"
    // in case of user not found, should throw error with the message "User not found"
    fun updateUser(token: String, userPatch: UserPatch): User =
        tokenRepository.getUserId(token)
            ?.let { userRepository.getUser(it) }
            ?.let { userDto ->
                userDto.copy(
                    email = userPatch.email ?: userDto.email,
                    name = userPatch.name ?: userDto.name,
                    surname = userPatch.surname ?: userDto.surname,
                )
            }
            ?.also {
                userRepository.updateUser(it)
                userByIdCache.remove(it.id)
                userByKeyCache.remove(it.key)
                logger.log("User updated: $it")
            }
            ?.toUser()
            ?: error("User not found")

    // Should add user to repository if token belongs to admin,
    // should log "User added: $user"
    fun addUser(token: String, addUser: AddUser): User {
        if (!tokenRepository.isAdmin(token)) error("Only admin can add user")
        return userDtoFactory.produceUserDto(addUser)
            .also(userRepository::addUser)
            .toUser()
            .also { logger.log("User added: $it") }
    }

    // Should return statistics about users
    // in case of token not belonging to admin, should throw error with the message "Only admin can get statistics"
    fun userStatistics(token: String): UserStatistics {
        if (!tokenRepository.isAdmin(token)) error("Only admin can get statistics")
        return UserStatistics(
            numberOfUsersCreatedEachDay = userRepository.getAllUsers()
                .groupingBy { it.creationTime.toLocalDate() }
                .eachCount()
        )
    }
    
    fun clearCache() {
        userByIdCache.clear()
        userByKeyCache.clear()
    }
}

class UserServiceTest {
    private val userRepository = FakeUserRepository()
    private val timeProvider = FakeTimeProvider()
    private val uuidGenerator = FakeUuidGenerator()
    private val userKeyGenerator = RealUserKeyGenerator(userRepository)
    private val userDtoFactory = UserDtoFactory(
        timeProvider = timeProvider,
        uuidGenerator = uuidGenerator,
        userKeyGenerator = userKeyGenerator,
    )
    private val tokenRepository = FakeTokenRepository()
    private val logger = FakeLogger()
    private val userService = UserService(userRepository, userDtoFactory, tokenRepository, logger)

    @After
    fun cleanup() {
        userRepository.cleanup()
        uuidGenerator.cleanup()
        timeProvider.cleanup()
        tokenRepository.cleanup()
        logger.cleanup()
        userService.clearCache()
    }   

    val user = User(
        id = "id",
        key = "key",
        email = "email",
        name = "name",
        surname = "surname",
        isAdmin = false,
        creationTime = LocalDateTime.of(2023, 10, 19, 13, 29, 49, 331810)
    )
    
    @Test
    fun `cache is well-configured`() {
        val userByIdCache = UserService::class.memberProperties
            .find { it.name == "userByIdCache" }
            ?.also { it.isAccessible = true }
            ?.get(userService) as Cache<*, *>
        
        val userByKeyCache = UserService::class.memberProperties
            .find { it.name == "userByKeyCache" }
            ?.also { it.isAccessible = true }
            ?.get(userService) as Cache<*, *>
        
        assertEquals(1.minutes, userByIdCache.clearAfterWrite as Duration)
        assertEquals(1.minutes, userByIdCache.clearAfterRead as Duration)
        assertEquals(1.minutes, userByKeyCache.clearAfterWrite as Duration)
        assertEquals(1.minutes, userByKeyCache.clearAfterRead as Duration)
    }

    @Test
    fun `should get user by id`() {
        // given
        userRepository.addUser(user.toUserDto("passwordHash"))

        // when
        val result = userService.getUser(user.id)

        // then
        assertEquals(user, result)
    }
    
    @Test
    fun `should use cache when getting user by id`() {
        // given
        userRepository.addUser(user.toUserDto("passwordHash"))
        val result1 = userService.getUser(user.id)
        val user2 = user.copy(name = "newName", surname = "newSurname")
        userRepository.addUser(user2.toUserDto("passwordHash"))

        // when
        val result2 = userService.getUser(user.id)

        // then
        assertEquals(user, result1)
        assertEquals(user, result2)
    }

    @Test
    fun `should get user by key`() {
        // given
        userRepository.addUser(user.toUserDto("passwordHash"))

        // when
        val result = userService.getUserByKey(user.key)

        // then
        assertEquals(user, result)
    }
    
    @Test
    fun `should use cache when getting user by key`() {
        // given
        userRepository.addUser(user.toUserDto("passwordHash"))
        val result1 = userService.getUser(user.id)
        val user2 = user.copy(name = "newName", surname = "newSurname")
        userRepository.addUser(user2.toUserDto("passwordHash"))

        // when
        val result2 = userService.getUserByKey(user.key)

        // then
        assertEquals(user, result1)
        assertEquals(user, result2)
    }

    @Test
    fun `should get token`() {
        // given
        userRepository.addUser(user.toUserDto("passwordHash"))

        // when
        val result = userService.getToken(user.email, "passwordHash")

        // then
        assertEquals(false, tokenRepository.isAdmin(result))
        assertEquals(user.id, tokenRepository.getUserId(result))
    }
    
    @Test
    fun `should throw error when getting token and password do not match`() {
        // given
        userRepository.addUser(user.toUserDto("passwordHash"))

        // when
        val result = runCatching { userService.getToken(user.email, "wrongPasswordHash") }

        // then
        assertEquals("Wrong email or password", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `should throw error when getting token and user does not exist`() {
        // when
        val result = runCatching { userService.getToken(user.email, "wrongPasswordHash") }

        // then
        assertEquals("Wrong email or password", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should update user`() {
        // given
        userRepository.addUser(user.toUserDto("passwordHash"))
        tokenRepository.shouldRespond("token", user.id, false)
        val userPatch = UserPatch(
            email = "newEmail",
            name = "newName",
            surname = "newSurname",
        )

        // when
        val result = userService.updateUser("token", userPatch)

        // then
        assertEquals(user.copy(email = "newEmail", name = "newName", surname = "newSurname"), result)
    }
    
    @Test
    fun `should clear cache when updating user`() {
        // given
        userRepository.addUser(user.toUserDto("passwordHash"))
        tokenRepository.shouldRespond("token", user.id, false)
        val userPatch = UserPatch(
            email = "newEmail",
            name = "newName",
            surname = "newSurname",
        )
        userRepository.addUser(user.toUserDto("passwordHash"))

        // when
        val result2 = userService.updateUser("token", userPatch)

        // then
        with(userService.getUser(user.id)) {
            assertEquals("newName", this?.name)
            assertEquals("newSurname", this?.surname)
        }
        with(userService.getUserByKey(user.key)) {
            assertEquals("newName", this?.name)
            assertEquals("newSurname", this?.surname)
        }
    }

    @Test
    fun `should add user`() {
        // given
        val addUser = AddUser(
            email = "email",
            name = "name",
            surname = "surname",
            passwordHash = "passwordHash",
        )
        val expected = User(
            id = "randomId",
            key = "namesurname",
            email = "email",
            name = "name",
            surname = "surname",
            isAdmin = false,
            creationTime = LocalDateTime.of(2020, 1, 1, 0, 0),
        )
        tokenRepository.shouldRespond("token", user.id, true)
        uuidGenerator.constantUuid = "randomId"

        // when
        val result = userService.addUser("token", addUser)

        // then
        assertEquals(expected, result)

        // and 
        assertEquals(expected, userService.getUser("randomId"))
    }

    @Test
    fun `should get user statistics`() {
        // given
        listOf(10, 20, 11, 27, 8).map { times ->
            repeat(times) {
                val user = user.toUserDto("passwordHash")
                    .copy(
                        id = uuidGenerator.generate(),
                        creationTime = timeProvider.now,
                        name = "name$it",
                        surname = "surname$it",
                    )
                userRepository.addUser(user)
            }
            timeProvider.now = timeProvider.now.plusDays(1)
        }
        tokenRepository.shouldRespond("token", user.id, true)

        // when
        val result = userService.userStatistics("token")

        // then
        assertEquals(
            UserStatistics(
                numberOfUsersCreatedEachDay = mapOf(
                    LocalDate.of(2020, 1, 1) to 10,
                    LocalDate.of(2020, 1, 2) to 20,
                    LocalDate.of(2020, 1, 3) to 11,
                    LocalDate.of(2020, 1, 4) to 27,
                    LocalDate.of(2020, 1, 5) to 8,
                )
            ),
            result
        )
    }
    
    @Test
    fun `should log when updating user`() {
        // given
        userRepository.addUser(user.toUserDto("passwordHash"))
        tokenRepository.shouldRespond("token", user.id, false)
        val userPatch = UserPatch(
            email = "newEmail",
            name = "newName",
            surname = "newSurname",
        )

        // when
        userService.updateUser("token", userPatch)

        // then
        assertEquals(
            listOf("User updated: UserDto(id=id, key=key, email=newEmail, name=newName, surname=newSurname, isAdmin=false, passwordHash=passwordHash, creationTime=2023-10-19T13:29:49.000331810)"),
            logger.messages
        )
    }
    
    @Test
    fun `should log when adding user`() {
        // given
        val addUser = AddUser(
            email = "email",
            name = "name",
            surname = "surname",
            passwordHash = "passwordHash",
        )
        tokenRepository.shouldRespond("token", user.id, true)
        uuidGenerator.constantUuid = "randomId"

        // when
        userService.addUser("token", addUser)

        // then
        assertEquals(
            listOf("User added: User(id=randomId, key=namesurname, email=email, name=name, surname=surname, isAdmin=false, creationTime=2020-01-01T00:00)"),
            logger.messages
        )
    }
}
