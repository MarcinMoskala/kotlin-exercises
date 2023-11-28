package user

import kotlinx.coroutines.*
import org.junit.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

class DiscUserRepository(private val discReader: DiscReader) : UserRepository {
    override suspend fun getUser(userId: String): UserData = UserData(discReader.read("user/$userId"))
}

interface DiscReader {
    fun read(key: String): String
}

interface UserRepository {
    suspend fun getUser(userId: String): UserData
}

data class UserData(val name: String)

@Suppress("FunctionName")
class DiscUserRepositoryTests {

    @Test
    fun `should read data from disc using DiscReader`() = runBlocking {
        val name = "Marcin"
        val repo = DiscUserRepository(OneSecDiscReader("Marcin"))
        val res = repo.getUser("SomeUserId")
        assertEquals(name, res.name)
    }

    class ImmediateDiscReader(val map: Map<String, String>) : DiscReader {
        override fun read(key: String): String = map[key] ?: error("Element not found")
    }

    @Test
    fun `should be prepared for many reads at the same time`() = runBlocking<Unit> {
        val repo = DiscUserRepository(OneSecDiscReader("Marcin"))
        val time = measureTimeMillis {
            coroutineScope {
                repeat(10) { id ->
                    launch {
                        repo.getUser("SomeUserId$id")
                    }
                }
            }
        }
        assert(time < 2000) { "Should take less than 2000, took $time" }
    }

    @Test
    fun `should be prepared for 200 reads at the same time`() = runBlocking<Unit> {
        val repo = DiscUserRepository(OneSecDiscReader("Marcin"))
        val time = measureTimeMillis {
            coroutineScope {
                repeat(200) { id ->
                    launch {
                        repo.getUser("SomeUserId$id")
                    }
                }
            }
        }
        assert(time < 2000) { "Should take less than 2000, took $time" }
    }

    class OneSecDiscReader(private val response: String) : DiscReader {
        override fun read(key: String): String {
            Thread.sleep(1000)
            return response
        }
    }
}
