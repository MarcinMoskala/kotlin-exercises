package advanced.reflection.mocking

import org.junit.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MockRegistry {
    private var recording = false
    private var bodyToStore: () -> Any? = {}
    private var handlers = mapOf<MockCall, () -> Any?>()

    inline fun <reified T> mock(): T = mock(T::class.java)

    fun <T> mock(clazz: Class<T>) = Proxy
        .newProxyInstance(
            clazz.classLoader,
            arrayOf(clazz),
            object : InvocationHandler {
                override fun invoke(
                    proxy: Any?, 
                    method: Method, 
                    args: Array<out Any>?
                ): Any? {
                    val call = MockCall(
                        mock = this,
                        method = method.name,
                        arguments = args?.toList()
                    )
                    if (recording) {
                        handlers += call to bodyToStore
                        throw RecordingCompleted()
                    } else {
                        val handler = handlers[call]
                        checkNotNull(handler) {
                            val argsStr = call.arguments
                                .orEmpty().joinToString()
                            "No handler for method " +
                                "${call.method}($argsStr)"
                        }
                        return handler.invoke()
                    }
                }
            }) as T

    fun <T> setReturnValue(rec: () -> T, value: T) {
        setBody(rec) { value }
    }

    fun <T> setBody(rec: () -> T, body: () -> T) {
        recording = true
        bodyToStore = body
        try {
            rec()
        } catch (e: RecordingCompleted) {
            // no-op
        } finally {
            recording = false
        }
    }

    private class RecordingCompleted : RuntimeException()
    
    private data class MockCall(
        val mock: Any,
        val method: String,
        val arguments: List<Any>?
    )
}

data class User(val name: String)

interface UserRepository {
    fun getUser(userId: String): User?
    fun allUsers(): List<User>
}

interface UserService {
    fun getUser(): User
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
    registry.setBody({ userRepository.allUsers() }) {
        listOf(User("James Bond"), User("Jane Doe"))
    }
    registry.setBody({ userService.getUser() }) {
        User(userRepository.getUser("dan")?.name ?: "Unknown")
    }

    println(userRepository.getUser("alex"))
    // User(name=Alex Smith)
    println(userRepository.allUsers())
    // [User(name=James Bond), User(name=Jane Doe)]
    println(userRepository.getUser("bell"))
    // User(name=Bell Rogers)
    println(userService.getUser())
    // User(name=Unknown)
    registry.setReturnValue(
        { userRepository.getUser("dan") },
        User("Dan Brown")
    )
    println(userService.getUser())
    // User(name=Dan Brown)
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
