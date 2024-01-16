package coroutines.test.notificationsender

import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Test
import kotlin.test.assertEquals

class NotificationSender(
    private val client: NotificationClient,
    private val exceptionCollector: ExceptionCollector,
    dispatcher: CoroutineDispatcher,
) {
    private val handler = CoroutineExceptionHandler { _, throwable -> 
        exceptionCollector.collectException(throwable)
    }
    val scope: CoroutineScope = CoroutineScope(dispatcher + SupervisorJob() + handler)

    fun sendNotifications(notifications: List<Notification>) {
        for (notification in notifications) {
            scope.launch { client.send(notification) }
        }
    }

    fun cancel() {
        scope.coroutineContext.cancelChildren()
    }
}

data class Notification(val id: String)

interface NotificationClient {
    suspend fun send(notification: Notification)
}

interface ExceptionCollector {
    fun collectException(throwable: Throwable)
}

class NotificationSenderTest {

    @Test
    fun `should send 20 notifications concurrently`() {
        
    }

    @Test
    fun `should support cancellation`() {
        
    }

    @Test
    fun `should not cancel other notifications, when one has exception`() {
        
    }

    @Test
    fun `should send info about failed notifications`() {
        
    }
}

class FakeNotificationClient(
    val delayTime: Long = 0L,
    val failEvery: Int = Int.MAX_VALUE
) : NotificationClient {
    var sent = emptyList<Notification>()
    var counter = 0
    var usedThreads = emptyList<String>()

    override suspend fun send(notification: Notification) {
        if (delayTime > 0) delay(delayTime)
        usedThreads += Thread.currentThread().name
        counter++
        if (counter % failEvery == 0) {
            throw FakeFailure(notification)
        }
        sent += notification
    }
}

class FakeFailure(val notification: Notification) : Throwable("Planned fail for notification ${notification.id}")

class FakeExceptionCollector : ExceptionCollector {
    var collected = emptyList<Throwable>()

    override fun collectException(throwable: Throwable) = synchronized(this) {
        collected += throwable
    }
}
