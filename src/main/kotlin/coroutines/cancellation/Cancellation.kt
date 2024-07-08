package coroutines.cancellation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
