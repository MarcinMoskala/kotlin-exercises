package functional.project

import org.junit.After
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

interface UserKeyGenerator {
    fun findPublicKey(name: String, surname: String): String?
}

class RealUserKeyGenerator(
    private val userRepository: UserRepository,
) : UserKeyGenerator {
    // Should find a key that is not used by any other user.
    // Should try the following combinations of name and surname:
    // - name + surname
    // - surname + name
    // - name + first letter of surname
    // - first letter of name + surname
    // - surname + first letter of name
    // - first letter of surname + name
    // Should remove all non-alphanumeric characters from the key.
    // Should lowercase the key.
    // Should trim the key.
    // Should return null if the key is shorter than 4 characters.
    // Should return null if key already exists in the database.
    override fun findPublicKey(name: String, surname: String): String? = TODO()
}

class FakeUserKeyGenerator : UserKeyGenerator {
    var constantKey: String? = null

    fun cleanup() {
        constantKey = null
    }

    override fun findPublicKey(name: String, surname: String): String? = constantKey
}

class RealUserKeyGeneratorTest {
    private val userRepository = FakeUserRepository()
    private val userKeyGenerator = RealUserKeyGenerator(userRepository)

    @After
    fun cleanup() {
        userRepository.cleanup()
    }

    @Test
    fun `should find public key`() {
        // when
        val result = userKeyGenerator.findPublicKey("name", "surname")

        // then
        assertEquals("namesurname", result)
    }

    @Test
    fun `should try all combinations`() {
        // given
        userRepository.allKeysAreUnavailable()

        // when
        val result = userKeyGenerator.findPublicKey("Michał", "Mazur")

        // then
        assertEquals(
            userRepository.checkedKeys, setOf(
                "michamazur",
                "mazurmicha",
                "micham",
                "mmazur",
                "mazurm",
                "mmicha",
            )
        )
    }
    
    @Test
    fun `should return null if key is shorter than 4 characters`() {
        // when
        val result = userKeyGenerator.findPublicKey("a", "b")

        // then
        assertEquals(null, result)
    }
    
    @Test
    fun `should return another option if key already exists in the database`() {
        // given
        userRepository.addUser(
            UserDto(
                id = "id",
                key = "namesurname",
                email = "email",
                name = "name",
                surname = "surname",
                isAdmin = false,
                passwordHash = "passwordHash",
                creationTime = LocalDateTime.of(2020, 1, 1, 0, 0),
            )
        )

        // when
        val result = userKeyGenerator.findPublicKey("name", "surname")

        // then
        assertEquals("surnamename", result)
    }
}
