package advanced.reflection.mocking

import org.junit.Test
import java.lang.RuntimeException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

data class User(val name: String)

interface UserRepository {
    fun getUser(userId: String): User?
    fun allUsers(): List<User>
}

interface UserService {
    fun getUser(): User
}

class MockRegistry {
    private val registry = mutableMapOf<CallIdentifier, (List<Any?>) -> Any?>()
    private var isRecording = false
    private var callIdentifier: CallIdentifier? = null

    inline fun <reified T : Any> mock(): T = mock(T::class.java) as T

    fun mock(clazz: Class<*>): Any {
        val identifier = Any()
        return Proxy.newProxyInstance(
            clazz.classLoader,
            arrayOf(clazz)
        ) { _, method, args ->
            val argsList = args?.toList().orEmpty()
            val id = CallIdentifier(identifier, method, argsList)
            if (isRecording) {
                callIdentifier = id
                throw RecordingException()
            } else {
                registry[id]?.invoke(argsList) ?: error("No handler for method ${method.name}(${argsList.joinToString()})")
            }
        }
    }
    
    fun <T> setReturnValue(recording: () -> T, value: T) {
        setBody(recording) { value }
    }

    fun <T> setBody(recording: () -> T, operation: (List<Any?>) -> T) {
        isRecording = true
        try {
            recording()
        } catch (e: RecordingException) {
            // no-op
        } finally {
            isRecording = false
            callIdentifier?.let {
                registry[it] = operation
                callIdentifier = null
            }
        }
    }

    data class CallIdentifier(val instance: Any?, val method: Method, val args: List<Any?>)
    class RecordingException : RuntimeException()
}

fun main() {
    val registry = MockRegistry()
    val userRepository = registry.mock<UserRepository>()
    val userService = registry.mock<UserService>()

    registry.setReturnValue(
        { userRepository.getUser("alex") },
        User("Alex Smith")
    )
    registry.setReturnValue(
        { userRepository.getUser("bell") },
        User("Bell Rogers")
    )
    registry.setReturnValue(
        { userRepository.getUser("dan") },
        null
    )
    println(userRepository.getUser("alex")) // User("Alex Smith")
    println(userRepository.getUser("bell")) // User("Bell Rogers")
    println(userRepository.getUser("dan")) // null
    
    registry.setBody({ userRepository.allUsers() }) {
        listOf(User("James Bond"), User("Jane Doe"))
    }
    registry.setBody({ userService.getUser() }) {
        User(userRepository.getUser("dan")?.name ?: "Unknown")
    }
    println(userRepository.allUsers()) // 
    println(userService.getUser()) // User(name=Dan Brown)
}

class MockingTest {
    interface GetValue {
        fun getInt(): Int
        fun getIntWithArg(arg: Int): Int
        fun getString(): String
    }

    @Test
    fun `should throw exception when no value set`() {
        val registry = MockRegistry()
        val mock = registry.mock<GetValue>()
        val result = runCatching {
            mock.getInt()
        }
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assert(exception is IllegalStateException)
        assertEquals("No handler for method getInt()", exception.message)

        val result2 = runCatching {
            mock.getIntWithArg(123)
        }
        val exception2 = result2.exceptionOrNull()
        assertNotNull(exception2)
        assert(exception2 is IllegalStateException)
        assertEquals("No handler for method getIntWithArg(123)", exception2.message)
    }

    @Test
    fun `should set int value`() {
        val registry = MockRegistry()
        val mock = registry.mock<GetValue>()
        registry.setReturnValue({ mock.getInt() }, 123)
        assertEquals(123, mock.getInt())
    }

    @Test
    fun `should set string value`() {
        val registry = MockRegistry()
        val mock = registry.mock<GetValue>()
        registry.setReturnValue({ mock.getString() }, "ABC")
        assertEquals("ABC", mock.getString())
    }

    @Test
    fun `should set int value with arg`() {
        val registry = MockRegistry()
        val mock = registry.mock<GetValue>()
        registry.setReturnValue({ mock.getIntWithArg(123) }, 456)
        registry.setReturnValue({ mock.getIntWithArg(456) }, 789)
        assertEquals(456, mock.getIntWithArg(123))
        assertEquals(789, mock.getIntWithArg(456))
    }

    @Test
    fun `should set constant int body`() {
        val registry = MockRegistry()
        val mock = registry.mock<GetValue>()
        registry.setBody({ mock.getInt() }) { 123 }
        assertEquals(123, mock.getInt())
    }

    @Test
    fun `should set int body`() {
        val registry = MockRegistry()
        val mock = registry.mock<GetValue>()
        var counter = 0
        registry.setBody({ mock.getInt() }) { counter++ }
        assertEquals(0, mock.getInt())
        assertEquals(1, mock.getInt())
        assertEquals(2, mock.getInt())
        assertEquals(3, mock.getInt())
        assertEquals(4, mock.getInt())
        assertEquals(5, mock.getInt())
    }

    @Test
    fun `should set constant string body`() {
        val registry = MockRegistry()
        val mock = registry.mock<GetValue>()
        registry.setBody({ mock.getString() }) { "ABC" }
        assertEquals("ABC", mock.getString())
    }

    @Test
    fun `should set string body`() {
        val registry = MockRegistry()
        val mock = registry.mock<GetValue>()
        var counter = 0
        registry.setBody({ mock.getString() }) { "ABC${counter++}" }
        assertEquals("ABC0", mock.getString())
        assertEquals("ABC1", mock.getString())
        assertEquals("ABC2", mock.getString())
        assertEquals("ABC3", mock.getString())
        assertEquals("ABC4", mock.getString())
        assertEquals("ABC5", mock.getString())
    }

    @Test
    fun `should set body depending on another mock`() {
        val registry = MockRegistry()
        val userRepository = registry.mock<UserRepository>()
        val userService = registry.mock<UserService>()

        registry.setReturnValue(
            { userRepository.getUser("alex") },
            User("Alex Smith")
        )
        registry.setReturnValue(
            { userRepository.getUser("bell") },
            User("Bell Rogers")
        )
        registry.setReturnValue(
            { userRepository.getUser("dan") },
            null
        )
        registry.setBody({ userRepository.allUsers() }) {
            listOf(User("James Bond"), User("Jane Doe"))
        }
        registry.setBody({ userService.getUser() }) {
            User(userRepository.getUser("dan")?.name ?: "Unknown")
        }

        assertEquals(User("Alex Smith"), userRepository.getUser("alex"))
        assertEquals(listOf(User("James Bond"), User("Jane Doe")), userRepository.allUsers())
        assertEquals(User("Bell Rogers"), userRepository.getUser("bell"))
        assertEquals(User("Unknown"), userService.getUser())
        registry.setReturnValue(
            { userRepository.getUser("dan") },
            User("Dan Brown")
        )
        assertEquals(User("Dan Brown"), userService.getUser())
    }
}
