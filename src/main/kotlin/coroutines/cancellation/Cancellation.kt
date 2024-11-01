@file:Suppress("unused")

package coroutines.cancellation

import kotlinx.coroutines.*
import java.io.File

suspend fun updateUser() {
    val user = readUser() // blocking
    val userSettings = readUserSettings(user.id) // blocking

    try {
        updateUserInDatabase(user, userSettings) // suspending
    } catch (e: CancellationException) {
        revertUnfinishedTransactions() // suspending
    }
}

// Secret functions
fun readUser(): User {
    return User("123", "John")
}

fun readUserSettings(userId: String): UserSettings {
    return UserSettings(userId, "en")
}

suspend fun updateUserInDatabase(user: User, userSettings: UserSettings) {
    println("Updating user in database: $user, $userSettings")
}

suspend fun revertUnfinishedTransactions() {
    println("Reverting unfinished transactions")
}

class User(val id: String, val name: String)
class UserSettings(val userId: String, val language: String)

// ****************************************************

suspend fun sendSignature(file: File) {
    try {
        val content = file.readText() // blocking
        val signature = calculateSignature(content) // regular function
        sendSignature(signature) // suspending
    } catch (e: Exception) {
        println("Error while sending signature: ${e.message}")
        e.printStackTrace()
    } finally {
        file.delete()
    }
}

// Secret functions
fun calculateSignature(content: String): String {
    return content.hashCode().toString()
}

suspend fun sendSignature(signature: String) {
    println("Signature sent: $signature")
}

// ****************************************************

suspend fun trySendUntilSuccess() {
    var success = false
    do {
        try {
            send()
            success = true
        } catch (e: Exception) {
            println("Error while sending: ${e.message}")
            e.printStackTrace()
        }
    } while (!success)
}

fun main() = runBlocking {
    val job = launch { trySendUntilSuccess() }
    delay(100)
    job.cancelAndJoin()
}

suspend fun send() {
    println("Sending...")
    delay(1000)
}

// ****************************************************

suspend fun storeUserArticles(userId: Int) {
    val token = getToken() // suspending
    val userDetails = fetchUserDetails(token, userId) // suspending
    val userArticles = fetchUserArticles(token, userId) // suspending

    for (article in userArticles.articles) {
        saveArticle(article, userDetails)
    }
    println("All articles saved")
}

fun saveArticle(article: Article, userDetails: UserDetails) {
    try {
        val articleContent = runBlocking { fetchArticle(article.key) } // suspending
        val articleFile = saveArticleToFile(articleContent) // blocking
        saveArticleMetadata(articleFile, articleContent, userDetails) // blocking
    } catch (e: Exception) {
        println("There was an exception while saving article $article:\n$e")
        println("Trying again...")
        saveArticle(article, userDetails) // recursive suspending
    }
}

//fun main() = runBlocking {
//    val job = launch { saveArticle(Article("A"), UserDetails(123)) }
//    delay(100)
//    job.cancelAndJoin()
//}

data class UserArticles(val articles: List<Article>)
data class Article(val key: String)
data class UserDetails(val userId: Int)

suspend fun getToken(): String = TODO()
suspend fun fetchUserDetails(token: String, userId: Int): UserDetails = TODO()
suspend fun fetchUserArticles(token: String, userId: Int): UserArticles = TODO()
suspend fun fetchArticle(key: String): String {
    delay(1000)
    return "Article content"
}

fun saveArticleMetadata(articleFile: File, articleContent: String, userDetails: UserDetails): Unit = TODO()
fun saveArticleToFile(articleContent: String): File = TODO()
suspend fun sendInformationAboutFailure(article: Article, e: Exception): Boolean = TODO()
