package advanced.reflection

import org.junit.Test
import utils.assertThrows
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.test.assertEquals
import kotlin.test.assertSame

// TODO: Implement Registry class and registry function here

data class UserConfiguration(val url: String)

interface UserRepository {
    fun get(): String
}

class RealUserRepository(
    private val userConfiguration: UserConfiguration,
) : UserRepository {
    override fun get(): String =
        "User from ${userConfiguration.url}"
}

class UserService(
    private val userRepository: UserRepository,
    private val userConfiguration: UserConfiguration,
) {
    fun get(): String = "Got ${userRepository.get()}"
}

fun main() {
//    val registry: Registry = registry {
//        singleton<UserConfiguration> {
//            UserConfiguration("http://localhost:8080")
//        }
//        register<UserService> {
//            UserService(
//                userRepository = get(),
//                userConfiguration = get(),
//            )
//        }
//        singleton<UserRepository> {
//            RealUserRepository(
//                userConfiguration = get(),
//            )
//        }
//    }
//
//    val userService: UserService = registry.get()
//    println(userService.get())
//    // Got User from http://localhost:8080
//
//    val ur1 = registry.get<UserRepository>()
//    val ur2 = registry.get<UserRepository>()
//    println(ur1 === ur2) // true
//
//    val uc1 = registry.get<UserService>()
//    val uc2 = registry.get<UserService>()
//    println(uc1 === uc2) // false
}

class RegistryTest {

//    @Test
//    fun `should get registered instance`() {
//        val registry = Registry()
//        registry.register(typeOf<String>()) { "ABC" }
//        assertEquals("ABC", registry.get<String>())
//    }
//
//    @Test
//    fun `should get registered instance with type`() {
//        val registry = Registry()
//        registry.register<String> { "ABC" }
//        assertEquals("ABC", registry.get<String>())
//    }
//
//    @Test
//    fun `should get registered single instance`() {
//        val registry = Registry()
//        registry.singleton(typeOf<String>()) { "ABC" }
//        assertEquals("ABC", registry.get<String>())
//    }
//
//    @Test
//    fun `should get registered single instance with type`() {
//        val registry = Registry()
//        registry.singleton<String> { "ABC" }
//        assertEquals("ABC", registry.get<String>())
//    }
//
//    @Test
//    fun `should return the same singleton instance`() {
//        val registry = Registry()
//
//        class A
//        registry.singleton(typeOf<A>()) { A() }
//        val instance1 = registry.get<A>()
//        val instance2 = registry.get<A>()
//        assertSame(instance1, instance2)
//    }
//
//    @Test
//    fun `should return the same singleton instance with type`() {
//        val registry = Registry()
//
//        class A
//        registry.singleton<A> { A() }
//        val instance1 = registry.get<A>()
//        val instance2 = registry.get<A>()
//        assertSame(instance1, instance2)
//    }
//
//    @Test
//    fun `should construct instance using registry`() {
//        val registry = Registry()
//
//        class B
//        class A(val b: B)
//        registry.register<A> { A(get()) }
//        registry.singleton<B> { B() }
//        val instance = registry.get<A>()
//        assertSame(instance.b, registry.get<B>())
//    }
//
//    @Test
//    fun `should respond to exists`() {
//        val registry = Registry()
//        registry.register<String> { "ABC" }
//        assertEquals(true, registry.exists<String>())
//        assertEquals(false, registry.exists<Int>())
//    }
//
//    @Test
//    fun `should respond to exists with type`() {
//        val registry = Registry()
//        registry.register<String> { "ABC" }
//        assertEquals(true, registry.exists(typeOf<String>()))
//        assertEquals(false, registry.exists(typeOf<Int>()))
//    }
//
//    @Test
//    fun `should throw exception when not exists`() {
//        val registry = Registry()
//        registry.register<String> { "ABC" }
//        assertThrows<IllegalArgumentException> {
//            registry.get<Int>()
//        }
//    }
//
//    @Test
//    fun `should throw exception when not exists with type`() {
//        val registry = Registry()
//        registry.register<String> { "ABC" }
//        assertThrows<IllegalArgumentException> {
//            registry.get(typeOf<Int>())
//        }
//    }
//
//    @Test
//    fun `should create instance using DSL`() {
//        val registry = registry {
//            register<String> { "ABC" }
//        }
//        assertEquals("ABC", registry.get<String>())
//    }
//
//    @Test
//    fun `should create user service`() {
//        val registry: Registry = registry {
//            singleton<UserConfiguration> {
//                UserConfiguration("http://localhost:8080")
//            }
//            register<UserService> {
//                UserService(
//                    userRepository = get(),
//                    userConfiguration = get(),
//                )
//            }
//            singleton<UserRepository> {
//                RealUserRepository(
//                    userConfiguration = get(),
//                )
//            }
//        }
//
//        val userService: UserService = registry.get()
//        assertEquals("Got User from http://localhost:8080", userService.get())
//
//        val ur1 = registry.get<UserRepository>()
//        val ur2 = registry.get<UserRepository>()
//        assert(ur1 === ur2)
//
//        val uc1 = registry.get<UserService>()
//        val uc2 = registry.get<UserService>()
//        assert(uc1 !== uc2)
//    }
}
