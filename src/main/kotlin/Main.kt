import com.google.errorprone.annotations.Immutable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@JvmInline value class UserId(val value: Int)
@JvmInline value class TransactionId(val value: Int)

fun main() {
    val duration: Duration = 1.hours + 30.minutes + 15.seconds
    setDuration(duration)
    
    fetchUser()
        .onSuccess { user -> println("User: $user") }
        .onFailure { error -> println("Error: $error") }
}

fun fetchUser(): Result<User> = Result.success(User(1, "Alice"))

data class User(val id: Int, val name: String)

fun setDuration(duration: Duration) {
    val javaDuration = duration.toJavaDuration()
    println(javaDuration)
}

