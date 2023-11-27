package functional.collections

import junit.framework.TestCase.assertEquals
import org.junit.Test

data class ArticleStatistics(
    val title: String,
    val views: Long,
)

class TopArticlesGenerator(
    private val articles: List<ArticleStatistics>,
) {
    // Should return top num articles according to their views,
    // but keep the original order of articles in the list
    fun topArticles(n: Int): List<ArticleStatistics> = TODO()
}

fun main() {
    val generator = TopArticlesGenerator(
        listOf(
            ArticleStatistics("Article 1", 400),
            ArticleStatistics("Article 2", 100),
            ArticleStatistics("Article 3", 200),
            ArticleStatistics("Article 4", 300),
            ArticleStatistics("Article 5", 500),
            ArticleStatistics("Article 6", 0),
        )
    )
    val topArticles = generator.topArticles(3)
    topArticles.onEach { println(it) }
    // ArticleStatistics(title=Article 1, views=400)
    // ArticleStatistics(title=Article 4, views=300)
    // ArticleStatistics(title=Article 5, views=500)
}

class TopArticlesTest {
    @Test
    fun `Top articles are returned in the correct order`() {
        val articles = listOf(
            ArticleStatistics("Article 1", 400),
            ArticleStatistics("Article 2", 100),
            ArticleStatistics("Article 3", 200),
            ArticleStatistics("Article 4", 300),
            ArticleStatistics("Article 5", 500),
            ArticleStatistics("Article 6", 0),
            
        )
        val generator = TopArticlesGenerator(articles)
        val topArticles = generator.topArticles(100)
        assertEquals(articles, topArticles)
    }
    
    @Test
    fun `Only n top articles are kept`() {
        val articles = listOf(
            ArticleStatistics("Article 1", 400),
            ArticleStatistics("Article 2", 100),
            ArticleStatistics("Article 3", 200),
            ArticleStatistics("Article 4", 300),
            ArticleStatistics("Article 5", 500),
            ArticleStatistics("Article 6", 0),
            
        )
        val generator = TopArticlesGenerator(articles)
        val topArticles = generator.topArticles(3)
        assertEquals(articles.slice(listOf(0, 3, 4)), topArticles)
    }
}
