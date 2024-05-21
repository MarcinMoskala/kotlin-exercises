package coroutines.dispatcher.discnewsrepository

import kotlinx.coroutines.*
import org.junit.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

class DiscNewsRepository(
    private val discReader: DiscReader
) : NewsRepository {
    override suspend fun getNews(newsId: String): News {
        val (title, content) = discReader.read("user/$newsId")
        return News(title, content)
    }
}

interface DiscReader {
    fun read(key: String): List<String>
}

interface NewsRepository {
    suspend fun getNews(newsId: String): News
}

data class News(val title: String, val content: String)

@Suppress("FunctionName")
class DiscNewsRepositoryTests {

    @Test
    fun `should read data from disc using DiscReader`() = runBlocking {
        val repo = DiscNewsRepository(OneSecDiscReader(listOf("Some title", "Some content")))
        val res = repo.getNews("SomeUserId")
        assertEquals("Some title", res.title)
        assertEquals("Some content", res.content)
    }

    class ImmediateDiscReader(val map: Map<String, List<String>>) : DiscReader {
        override fun read(key: String): List<String> = map[key] ?: error("Element not found")
    }

    @Test
    fun `should be prepared for many reads at the same time`() = runBlocking<Unit> {
        val repo = DiscNewsRepository(OneSecDiscReader(listOf("Some title", "Some content")))
        val time = measureTimeMillis {
            coroutineScope {
                repeat(10) { id ->
                    launch {
                        repo.getNews("SomeUserId$id")
                    }
                }
            }
        }
        assert(time < 2000) { "Should take less than 2000, took $time" }
    }

    @Test(timeout = 2000)
    fun `should be prepared for 200 parallel reads`() = runBlocking<Unit> {
        val repo = DiscNewsRepository(OneSecDiscReader(listOf("Some title", "Some content")))
        val time = measureTimeMillis {
            coroutineScope {
                repeat(200) { id ->
                    launch {
                        repo.getNews("SomeUserId$id")
                    }
                }
            }
        }
        assert(time < 1900) { "Should take less than 2000, took $time" }
    }

    @Test(timeout = 3000)
    fun `should not allow more than 200 parallel reads`() = runBlocking<Unit> {
        val repo = DiscNewsRepository(OneSecDiscReader(listOf("Some title", "Some content")))
        val time = measureTimeMillis {
            coroutineScope {
                repeat(201) { id ->
                    launch {
                        repo.getNews("SomeUserId$id")
                    }
                }
            }
        }
        assert(time < 2000) { "Should take less than 2000, took $time" }
    }

    class OneSecDiscReader(private val response: List<String>) : DiscReader {
        override fun read(key: String): List<String> {
            Thread.sleep(1000)
            return response
        }
    }
}
