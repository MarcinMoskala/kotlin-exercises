import kotlinx.coroutines.*
import java.lang.IllegalStateException
import kotlin.coroutines.coroutineContext

fun main() = runBlocking(CoroutineName("AAA")) {
    val tweets = async { getTweets() }
    val details = try {
        getUserDetails()
    } catch (e: IllegalStateException) {
        null
    }
    println("User: $details")
    println("Tweets: ${tweets.await()}")
}

suspend fun getUserDetails(): Details = coroutineScope {
    val userName = getUserName()
    val followersNumber = getFollowersNumber()
    Details(userName, followersNumber)
}

data class Details(val name: String, val followers: Int)
data class Tweet(val text: String)

suspend fun getFollowersNumber(): Int {
    delay(1000)
//    error("No internet connection")
    return 42
}

suspend fun getUserName(): String {
    delay(1500)
    return "marcinmoskala${coroutineContext[CoroutineName]?.name}"
}

suspend fun getTweets(): List<Tweet> {
    delay(2000)
    return listOf(Tweet("Hello, world"))
}
