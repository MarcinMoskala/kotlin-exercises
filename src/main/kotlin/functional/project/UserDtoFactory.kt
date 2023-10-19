package functional.project

import junit.framework.TestCase
import org.junit.After
import org.junit.Test
import java.time.LocalDateTime

class UserDtoFactory(
    private val timeProvider: TimeProvider,
    private val uuidGenerator: UuidGenerator,
    private val userKeyGenerator: UserKeyGenerator,
) {
    // Should produce UserDto based on AddUser
    // Should generate id using uuidGenerator
    // Should generate key using userKeyGenerator, or generate random key using uuidGenerator if userKeyGenerator returns null
    // Should set isAdmin to false
    // Should set creationTime to current time using timeProvider
    fun produceUserDto(addUser: AddUser): UserDto = TODO()
}

class UserDtoFactoryTest {
    private val timeProvider = FakeTimeProvider()
    private val uuidGenerator = FakeUuidGenerator()
    private val userKeyGenerator = FakeUserKeyGenerator()
    private val userDtoFactory = UserDtoFactory(timeProvider, uuidGenerator, userKeyGenerator)

    @After
    fun cleanup() {
        timeProvider.cleanup()
        uuidGenerator.cleanup()
        userKeyGenerator.cleanup()
    }

    @Test
    fun `should produce user dto`() {
        // given
        val addUser = AddUser(
            email = "email",
            name = "name",
            surname = "surname",
            passwordHash = "passwordHash",
        )
        val expected = UserDto(
            id = "randomId",
            key = "namesurname",
            email = "email",
            name = "name",
            surname = "surname",
            isAdmin = false,
            passwordHash = "passwordHash",
            creationTime = LocalDateTime.of(2020, 1, 1, 0, 0),
        )
        uuidGenerator.constantUuid = "randomId"
        userKeyGenerator.constantKey = "namesurname"
        timeProvider.now = LocalDateTime.of(2020, 1, 1, 0, 0)

        // when
        val result = userDtoFactory.produceUserDto(addUser)

        // then
        TestCase.assertEquals(expected, result)
    }
}
