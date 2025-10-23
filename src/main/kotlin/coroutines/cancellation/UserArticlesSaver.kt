package coroutines.cancellation.userarticlessaver

import kotlinx.coroutines.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class UserArticlesSaver(
    private val tokenProvider: TokenProvider,
    private val client: Client,
    private val storage: Storage,
    private val logger: Logger,
) {
    suspend fun storeUserArticles(userId: Int) {
        val token = tokenProvider.getToken() // suspending
        val userDetails = client.fetchUserDetails(token, userId) // suspending
        val userArticles = client.fetchUserArticles(token, userId) // suspending

        for (article in userArticles.articles) {
            saveArticle(article, userDetails)
        }
        client.notifyAllArticlesSaved(userId)
    }

    private fun saveArticle(article: Article, userDetails: UserDetails) {
        try {
            val articleContent = runBlocking { client.fetchArticle(article.key) } // suspending
            val articleFile = storage.saveArticleToFile(articleContent) // blocking
            storage.saveArticleMetadata(articleFile, articleContent, userDetails) // blocking
        } catch (e: Exception) {
            logger.log("Exception while saving article $article", e)
            saveArticle(article, userDetails) // recursive suspending
        }
    }
}

interface TokenProvider {
    suspend fun getToken(): String
}
interface Client {
    suspend fun fetchUserDetails(token: String, userId: Int): UserDetails
    suspend fun fetchUserArticles(token: String, userId: Int): UserArticles
    suspend fun fetchArticle(key: String): String
    suspend fun sendInformationAboutFailure(article: Article, e: Exception): Boolean
    suspend fun notifyAllArticlesSaved(userId: Int)
}
interface Storage {
    fun saveArticleMetadata(articleFile: File, articleContent: String, userDetails: UserDetails)
    fun saveArticleToFile(articleContent: String): File
}
interface Logger {
    fun log(message: String, e: Exception? = null)
}
data class UserArticles(val articles: List<Article>)
data class Article(val key: String)
data class UserDetails(val userId: Int)

class UserArticlesSaverTest {
    @Test
    fun `should fetch user details and articles concurrently`() = runTest {
        // Given
        val tokenProvider = FakeTokenProvider()
        val client = object : FakeClient() {
            override suspend fun fetchUserDetails(token: String, userId: Int): UserDetails {
                delay(100)
                return UserDetails(userId)
            }

            override suspend fun fetchUserArticles(token: String, userId: Int): UserArticles {
                delay(100)
                return UserArticles(emptyList())
            }
        }
        val storage = FakeStorage()
        val logger = FakeLogger()
        val saver = UserArticlesSaver(tokenProvider, client, storage, logger)

        // When
        saver.storeUserArticles(1)

        // Then - both should start at the same time and overlap
        assert(currentTime == 100L) {
            "User details and articles fetching should be concurrent"
        }
    }

    @Test
    fun `should save articles concurrently`() = runTest {
        // Given
        val startTimes = ConcurrentHashMap<String, Long>()
        val endTimes = ConcurrentHashMap<String, Long>()

        val articles = listOf(Article("key1"), Article("key2"), Article("key3"))
        val tokenProvider = FakeTokenProvider()
        val client = object : FakeClient() {
            override suspend fun fetchUserArticles(token: String, userId: Int) =
                UserArticles(articles)

            override suspend fun fetchArticle(key: String): String {
                startTimes[key] = currentTime
                delay(100)
                endTimes[key] = currentTime
                return "content"
            }
        }
        val storage = FakeStorage()
        val logger = FakeLogger()
        val saver = UserArticlesSaver(tokenProvider, client, storage, logger)

        // When
        saver.storeUserArticles(1)

        // Then - all articles should start at the same time
        assertEquals(3, startTimes.size)
        val firstStart = startTimes.values.first()
        assert(startTimes.values.all { it == firstStart })
    }

    @Test
    fun `should notify articles completion after all articles sent`() = runTest {
        // Given
        val events = mutableListOf<String>()
        val articles = listOf(Article("key1"), Article("key2"))

        val tokenProvider = FakeTokenProvider()
        val client = object : FakeClient() {
            override suspend fun fetchUserArticles(token: String, userId: Int) =
                UserArticles(articles)

            override suspend fun fetchArticle(key: String): String {
                delay(50)
                events.add("article-$key-fetched")
                return "content"
            }

            override suspend fun notifyAllArticlesSaved(userId: Int) {
                events.add("notified")
            }
        }
        val storage = object : FakeStorage() {
            override fun saveArticleMetadata(articleFile: File, articleContent: String, userDetails: UserDetails) {
                events.add("metadata-saved")
            }
        }
        val logger = FakeLogger()
        val saver = UserArticlesSaver(tokenProvider, client, storage, logger)

        // When
        saver.storeUserArticles(1)

        // Then
        assert(events.contains("notified"))
        val notifyIndex = events.indexOf("notified")
        val metadataSavedCount = events.count { it == "metadata-saved" }
        assertEquals(2, metadataSavedCount)
        assert(events.take(notifyIndex).count { it == "metadata-saved" } == 2) {
            "All metadata should be saved before notification"
        }
    }

    @Test
    fun `should keep structured concurrency`() = runTest {
        // Given
        val name = CoroutineName("test-coroutine")
        var suspendingCallNames = listOf<CoroutineName?>()
        suspend fun addNameToList() {
            suspendingCallNames += currentCoroutineContext()[CoroutineName]
        }

        val tokenProvider = FakeTokenProvider()
        val client = object : FakeClient() {
            override suspend fun fetchUserDetails(token: String, userId: Int): UserDetails {
                addNameToList()
                return super.fetchUserDetails(token, userId)
            }

            override suspend fun fetchUserArticles(token: String, userId: Int): UserArticles {
                addNameToList()
                return UserArticles(listOf(Article("key1"), Article("key2")))
            }

            override suspend fun fetchArticle(key: String): String {
                addNameToList()
                return super.fetchArticle(key)
            }

            override suspend fun notifyAllArticlesSaved(userId: Int) {
                addNameToList()
                super.notifyAllArticlesSaved(userId)
            }

            override suspend fun sendInformationAboutFailure(article: Article, e: Exception): Boolean {
                addNameToList()
                return super.sendInformationAboutFailure(article, e)
            }
        }
        val storage = FakeStorage()
        val logger = FakeLogger()
        val saver = UserArticlesSaver(tokenProvider, client, storage, logger)

        // When
        withContext(name) {
            saver.storeUserArticles(1)
        }

        // Then - should use virtual time, not block
        assert(suspendingCallNames.all { it == name }) {
            "All suspending calls should inherit coroutine name"
        }
        assert(suspendingCallNames.size == 5) {
            "All 5 suspending calls should be made"
        }
    }

    @Test
    fun `should allow cancellation between saving article and metadata`() = runTest {
        // Given
        val articles = listOf(Article("key1"))
        var metadataSaved = false
        var job: Job? = null

        val tokenProvider = FakeTokenProvider()
        val client = object : FakeClient() {
            override suspend fun fetchUserArticles(token: String, userId: Int) =
                UserArticles(articles)
        }
        val storage = object : FakeStorage() {
            override fun saveArticleToFile(articleContent: String): File {
                job?.cancel()
                return super.saveArticleToFile(articleContent)
            }

            override fun saveArticleMetadata(articleFile: File, articleContent: String, userDetails: UserDetails) {
                metadataSaved = true
            }
        }
        val logger = FakeLogger()
        val saver = UserArticlesSaver(tokenProvider, client, storage, logger)

        // When
        job = launch {
            saver.storeUserArticles(1)
        }
        job.join()

        // Then - metadata should not be saved if cancelled after yield
        assert(!metadataSaved)
        assert(job.isCancelled)
    }

    @Test
    fun `should not stop cancellation exception`() = runTest {
        // Given
        val articles = listOf(Article("key1"))

        val tokenProvider = FakeTokenProvider()
        val client = object : FakeClient() {
            override suspend fun fetchUserArticles(token: String, userId: Int) =
                UserArticles(articles)

            override suspend fun fetchArticle(key: String): String {
                delay(100)
                return "content"
            }
        }
        val storage = FakeStorage()
        val logger = FakeLogger()
        val saver = UserArticlesSaver(tokenProvider, client, storage, logger)

        // When/Then
        val job = launch {
            saver.storeUserArticles(1)
        }

        delay(10)
        job.cancel()
        job.join()

        assert(job.isCancelled)
    }

    @Test
    fun `should log cancellation exception if cancelled while fetching article`() = runTest {
        // Given
        val articles = listOf(Article("key1"))

        val tokenProvider = FakeTokenProvider()
        val client = object : FakeClient() {
            override suspend fun fetchUserArticles(token: String, userId: Int) =
                UserArticles(articles)

            override suspend fun fetchArticle(key: String): String {
                delay(100)
                throw CancellationException("Cancelled")
            }
        }
        val storage = FakeStorage()
        val logger = FakeLogger()
        val saver = UserArticlesSaver(tokenProvider, client, storage, logger)

        // When
        val job = launch {
            try {
                saver.storeUserArticles(1)
            } catch (e: CancellationException) {
            }
        }

        delay(10)
        job.cancel()
        job.join()

        // Then - CancellationException should not be logged (it's rethrown)
        assertEquals(
            "CancellationException should not be logged",
            0, logger.loggedExceptions.size,
        )
    }

    @Test
    fun `should log exceptions from saving`() = runTest {
        // Given
        val articles = listOf(Article("key1"))
        val exception = RuntimeException("Save failed")
        var attemptCount = 0

        val tokenProvider = FakeTokenProvider()
        val client = object : FakeClient() {
            override suspend fun fetchUserArticles(token: String, userId: Int) =
                UserArticles(articles)

            override suspend fun fetchArticle(key: String): String {
                attemptCount++
                if (attemptCount <= 2) {
                    throw exception
                }
                return "content"
            }
        }
        val storage = FakeStorage()
        val logger = FakeLogger()
        val saver = UserArticlesSaver(tokenProvider, client, storage, logger)

        // When
        saver.storeUserArticles(1)

        // Then
        assert(logger.loggedExceptions.size >= 2)
        assert(logger.loggedExceptions.all { it == exception })
    }

    @Test
    fun `should retry saving article on failure`() = runTest {
        // Given
        val articles = listOf(Article("key1"))
        val attemptCount = AtomicInteger(0)

        val tokenProvider = FakeTokenProvider()
        val client = object : FakeClient() {
            override suspend fun fetchUserArticles(token: String, userId: Int) =
                UserArticles(articles)

            override suspend fun fetchArticle(key: String): String {
                val attempt = attemptCount.incrementAndGet()
                if (attempt < 3) {
                    throw RuntimeException("Temporary failure")
                }
                return "content"
            }
        }
        val storage = FakeStorage()
        val logger = FakeLogger()
        val saver = UserArticlesSaver(tokenProvider, client, storage, logger)

        // When
        saver.storeUserArticles(1)

        // Then
        assertEquals("Should retry until success", 3, attemptCount.get())
    }

    @Test
    fun `should not retry saving article on cancellation`() = runTest {
        // Given
        val articles = listOf(Article("key1"))
        val attemptCount = AtomicInteger(0)

        val tokenProvider = FakeTokenProvider()
        val client = object : FakeClient() {
            override suspend fun fetchUserArticles(token: String, userId: Int) =
                UserArticles(articles)

            override suspend fun fetchArticle(key: String): String {
                attemptCount.incrementAndGet()
                delay(100)
                throw CancellationException("Cancelled")
            }
        }
        val storage = FakeStorage()
        val logger = FakeLogger()
        val saver = UserArticlesSaver(tokenProvider, client, storage, logger)

        // When
        val job = launch {
            try {
                saver.storeUserArticles(1)
            } catch (e: CancellationException) {
            }
        }

        delay(50)
        job.cancel()
        job.join()

        // Then - should not retry on cancellation
        assert(attemptCount.get() <= 2)
    }
}

private class FakeTokenProvider : TokenProvider {
    override suspend fun getToken() = "test-token"
}

private open class FakeClient : Client {
    override suspend fun fetchUserDetails(token: String, userId: Int) = UserDetails(userId)
    override suspend fun fetchUserArticles(token: String, userId: Int) = UserArticles(emptyList())
    override suspend fun fetchArticle(key: String) = "content-$key"
    override suspend fun sendInformationAboutFailure(article: Article, e: Exception) = true
    override suspend fun notifyAllArticlesSaved(userId: Int) {}
}

private open class FakeStorage : Storage {
    override fun saveArticleMetadata(articleFile: File, articleContent: String, userDetails: UserDetails) {}
    override fun saveArticleToFile(articleContent: String) = File("fake-file")
}

private class FakeLogger : Logger {
    val loggedExceptions = mutableListOf<Exception>()

    override fun log(message: String, e: Exception?) {
        e?.let { loggedExceptions.add(it) }
    }
}
