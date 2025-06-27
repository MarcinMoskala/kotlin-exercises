package comment

import coroutines.comment.EmailService
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class FakeEmailService : EmailService {
    private var emailsSent = mutableListOf<Pair<String, String>>()
    var notificationDelay: Long = 1000 // Default delay of 1 second
    private val concurrentNotifications = AtomicInteger(0)
    private var maxConcurrentNotifications = 0
    private val notificationStarted = AtomicBoolean(false)
    private val notificationCompleted = AtomicBoolean(false)

    override suspend fun notifyAboutCommentInObservedCollection(email: String, collectionKey: String, comment: String?) {
        notificationStarted.set(true)
        val currentConcurrent = concurrentNotifications.incrementAndGet()

        // Update max concurrent notifications
        synchronized(this) {
            if (currentConcurrent > maxConcurrentNotifications) {
                maxConcurrentNotifications = currentConcurrent
            }
        }

        // Simulate work with delay
        delay(notificationDelay)

        val body = "New comment in collection $collectionKey: $comment"

        // Add the email to the list of sent emails
        synchronized(this) {
            emailsSent.add(email to body)
        }

        concurrentNotifications.decrementAndGet()
        notificationCompleted.set(true)
    }

    fun getEmailsSent(): List<Pair<String, String>> = emailsSent.toList()

    fun getMaxConcurrentNotifications(): Int = maxConcurrentNotifications

    fun isNotificationStarted(): Boolean = notificationStarted.get()

    fun isNotificationCompleted(): Boolean = notificationCompleted.get()

    fun clear() {
        synchronized(this) {
            emailsSent.clear()
        }
        maxConcurrentNotifications = 0
        concurrentNotifications.set(0)
        notificationStarted.set(false)
        notificationCompleted.set(false)
    }
}
