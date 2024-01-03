package coroutines.starting.articleservice

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDateTime

class ArticleService(
    private val articleRepository: ArticleRepository,
    private val userService: UserService,
    private val timeProvider: TimeProvider,
) {
    suspend fun getArticles(
        requestAuth: RequestAuth? = null,
        series: ArticleSeries? = null,
    ): List<Article> = coroutineScope {
        val articles = async { articleRepository.getArticles() }
        val currentUser = userService.findUser(requestAuth)
        val time = timeProvider.now()
        articles.await()
            .filter { series == null || it.series == series }
            .filter { canSeeOnList(currentUser, it, time) }
    }

    private fun canSeeOnList(
        user: User?,
        article: Article,
        time: LocalDateTime
    ): Boolean = when {
        article.isPublic && time >= article.releaseDate -> true
        user?.isAdmin == true -> true
        user?.key == article.authorKey -> true
        else -> false
    }
}

interface ArticleRepository {
    suspend fun getArticles(): List<Article>
}

interface UserService {
    suspend fun findUser(requestAuth: RequestAuth?): User?
}

interface TimeProvider {
    fun now(): LocalDateTime
}

class User(val key: String, val isAdmin: Boolean)
class RequestAuth(val token: String)

data class ArticleSeries(val key: String)
data class Article(
    val key: String,
    val title: String,
    val content: String,
    val authorKey: String,
    val isPublic: Boolean,
    val series: ArticleSeries?,
    val releaseDate: LocalDateTime,
)

data class Date(val toEpochSecond: () -> Long)
