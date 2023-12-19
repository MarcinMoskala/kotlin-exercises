import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val tweets = async { getTweets() }
    val details = try {
        getUserDetails()
    } catch (e: Error) {
        null
    }
    println("User: $details")
    println("Tweets: ${tweets.await()}")
}

suspend fun getUserDetails(): Details {
    val userName = getUserName()
    val followersNumber = getFollowersNumber()
    return Details(userName, followersNumber)
}

data class Details(val name: String, val followers: Int)
data class Tweet(val text: String)

suspend fun getFollowersNumber(): Int {
    delay(1000)
    return 42
}

suspend fun getUserName(): String {
    delay(1500)
    return "marcinmoskala"
}

suspend fun getTweets(): List<Tweet> {
    delay(2000)
    return listOf(Tweet("Hello, world"))
}
