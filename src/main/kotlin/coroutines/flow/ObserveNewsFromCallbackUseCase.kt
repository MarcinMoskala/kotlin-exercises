package coroutines.flow.observenewsfromcallbackusecase

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.*

class ObserveNewsFromCallbackUseCase(
    private val repository: NewsRepository,
) {
    fun observeNews(): Flow<News> = TODO()
}

data class News(
    val id: String,
    val title: String,
)

interface NewsRepository {
    suspend fun collectBreakingNews(emit: suspend (News) -> Unit)
    suspend fun collectRecommendedNews(emit: suspend (News) -> Unit)
}

class FakeNewsRepository : NewsRepository {
    private val breaking = Channel<News>(Channel.UNLIMITED)
    private val recommended = Channel<News>(Channel.UNLIMITED)

    var breakingCollectors = 0
    var recommendedCollectors = 0

    override suspend fun collectBreakingNews(emit: suspend (News) -> Unit) {
        breakingCollectors++
        try {
            for (news in breaking) {
                emit(news)
            }
        } finally {
            breakingCollectors--
        }
    }

    override suspend fun collectRecommendedNews(emit: suspend (News) -> Unit) {
        recommendedCollectors++
        try {
            for (news in recommended) {
                emit(news)
            }
        } finally {
            recommendedCollectors--
        }
    }

    suspend fun emitBreaking(news: News) {
        breaking.send(news)
    }

    suspend fun emitRecommended(news: News) {
        recommended.send(news)
    }

    fun closeStreams() {
        breaking.close()
        recommended.close()
    }
}

class ObserveNewsFromCallbackUseCaseTest {

    @Test
    fun `observeNews launches collection from both sources`() = runTest {
        val repository = FakeNewsRepository()
        val useCase = ObserveNewsFromCallbackUseCase(repository)

        val job = launch {
            useCase.observeNews().collect { }
        }

        withTimeout(1_000) {
            while (repository.breakingCollectors == 0 || repository.recommendedCollectors == 0) {
                kotlinx.coroutines.yield()
            }
        }
        assertEquals(1, repository.breakingCollectors)
        assertEquals(1, repository.recommendedCollectors)

        job.cancelAndJoin()
    }

    @Test
    fun `observeNews forwards items from both flows`() = runTest {
        val repository = FakeNewsRepository()
        val useCase = ObserveNewsFromCallbackUseCase(repository)

        val expected = listOf(
            News("1", "Breaking: Kotlin 2.4 released"),
            News("2", "Recommended: Coroutines guide updated"),
        )

        val collected = async {
            useCase.observeNews().take(2).toList()
        }

        repository.emitBreaking(expected[0])
        repository.emitRecommended(expected[1])

        assertContentEquals(expected, collected.await())
    }

    @Test
    fun `observeNews propagates errors from source flows`() = runTest {
        val error = IllegalStateException("breaking stream failed")
        val repository = object : NewsRepository {
            override suspend fun collectBreakingNews(emit: suspend (News) -> Unit) {
                throw error
            }

            override suspend fun collectRecommendedNews(emit: suspend (News) -> Unit) {
                kotlinx.coroutines.awaitCancellation()
            }
        }
        val useCase = ObserveNewsFromCallbackUseCase(repository)

        val thrown = assertFailsWith<IllegalStateException> {
            useCase.observeNews().first()
        }
        assertEquals("breaking stream failed", thrown.message)
    }

    @Test
    fun `observeNews cancels source collectors when downstream is cancelled`() = runTest {
        val repository = FakeNewsRepository()
        val useCase = ObserveNewsFromCallbackUseCase(repository)

        val job = launch {
            useCase.observeNews().collect { }
        }

        withTimeout(1_000) {
            while (repository.breakingCollectors == 0 || repository.recommendedCollectors == 0) {
                kotlinx.coroutines.yield()
            }
        }
        assertTrue(repository.breakingCollectors > 0)
        assertTrue(repository.recommendedCollectors > 0)

        job.cancelAndJoin()
        repository.closeStreams()

        assertEquals(0, repository.breakingCollectors)
        assertEquals(0, repository.recommendedCollectors)
    }
}
