package coroutines.starting.notificationsender

import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Test
import kotlin.test.assertEquals

class NotificationSenderTask(
    val notificationRepository: NotificationRepository,
    val notificationSender: NotificationSender,
    val backgroundScope: CoroutineScope,
) {

    @Cron("0 0/1 * * * ?")
    fun sendNotifications() {
        backgroundScope.launch {
            val notificationsToSend = notificationRepository.getPendingNotifications()
            notificationsToSend.map {
                async {
                    notificationSender.send(it)
                }
            }.awaitAll().zip(notificationsToSend)
                .filter { it.first }
                .map { it.second.id }
                .let { notificationRepository.markAsSent(it) }
        }
    }
}

interface NotificationRepository {
    suspend fun getPendingNotifications(): List<Notification>
    suspend fun markAsSent(notificationIds: List<String>)
}

interface NotificationSender {
    suspend fun send(notification: Notification): Boolean
}

data class Notification(val id: String, val message: String)

annotation class Cron(val expression: String)

class NotificationSenderTaskTest {

    @Test
    fun `should send notifications concurrently using virtual time`() {
        // given
        val testDispatcher = StandardTestDispatcher()
        val sendingTime = 101L
        val markAsSentTime = 102L
        val getPendingNotificationTime = 103L
        val fakeRepository = FakeNotificationRepository(
            getPendingNotificationTime = getPendingNotificationTime,
            markAsSentTime = markAsSentTime,
        )
        val fakeSender = FakeNotificationSender(sendingTime = sendingTime)
        val task = NotificationSenderTask(
            notificationRepository = fakeRepository,
            notificationSender = fakeSender,
            backgroundScope = CoroutineScope(testDispatcher)
        )

        // Add some pending notifications
        val notifications = List(5) { Notification("ID$it", "Message $it") }
        fakeRepository.setPendingNotifications(notifications)

        // when
        task.sendNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        // then
        // All notifications should be sent
        assertEquals(notifications.size, fakeSender.sentNotifications.size, "All notifications should be sent")
        val expectedTimeIfAsynchronous = sendingTime + markAsSentTime + getPendingNotificationTime
        val expectedTimeIfSynchronous = sendingTime + getPendingNotificationTime + markAsSentTime * 5
        val actualTime = testDispatcher.scheduler.currentTime
        assertEquals(
            sendingTime + markAsSentTime + getPendingNotificationTime, actualTime,
            "Total time should be the sum of sending, marking as sent, and getting pending notifications, so $expectedTimeIfAsynchronous, not it is $actualTime" + if (actualTime == expectedTimeIfSynchronous) " what means that tasks were executed synchronously" else ""
        )
    }

    @Test
    fun `should send notifications asynchronously`() {
        // given
        val testDispatcher = StandardTestDispatcher()
        val fakeRepository = FakeNotificationRepository()
        val fakeSender = FakeNotificationSender()
        val task = NotificationSenderTask(
            notificationRepository = fakeRepository,
            notificationSender = fakeSender,
            backgroundScope = CoroutineScope(testDispatcher)
        )

        // Add some pending notifications
        val notifications = List(5) { Notification("ID$it", "Message $it") }
        fakeRepository.setPendingNotifications(notifications)

        // when
        task.sendNotifications()

        // then - before advancing time, nothing should be sent yet
        assertEquals(0, fakeSender.sentNotifications.size, "No notifications should be sent before advancing time")
        assertEquals(
            0,
            fakeRepository.markedAsSent.size,
            "No notifications should be marked as sent before advancing time"
        )

        // Advance time to allow coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()

        // then - after advancing time, all notifications should be sent
        assertEquals(notifications.size, fakeSender.sentNotifications.size, "All notifications should be sent")
        assertEquals(notifications, fakeSender.sentNotifications, "All notifications should be sent in order")
    }

    @Test
    fun `should mark successful notifications as sent`() {
        // given
        val testDispatcher = StandardTestDispatcher()
        val fakeRepository = FakeNotificationRepository()
        val fakeSender = FakeNotificationSender(successfulIds = setOf("ID0", "ID2", "ID4"))
        val task = NotificationSenderTask(
            notificationRepository = fakeRepository,
            notificationSender = fakeSender,
            backgroundScope = CoroutineScope(testDispatcher)
        )

        // Add some pending notifications
        val notifications = List(5) { Notification("ID$it", "Message $it") }
        fakeRepository.setPendingNotifications(notifications)

        // when
        task.sendNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        // then
        assertEquals(5, fakeSender.sentNotifications.size, "All notifications should be sent")
        assertEquals(3, fakeRepository.markedAsSent.size, "Only successful notifications should be marked as sent")
        assertEquals(
            listOf("ID0", "ID2", "ID4"),
            fakeRepository.markedAsSent,
            "Correct notification IDs should be marked as sent"
        )
    }
}

class FakeNotificationRepository(
    private val getPendingNotificationTime: Long = 0L,
    private val markAsSentTime: Long = 0L
) : NotificationRepository {
    private var pendingNotifications: List<Notification> = emptyList()
    var markedAsSent: List<String> = emptyList()

    // Method to set pending notifications for testing
    fun setPendingNotifications(notifications: List<Notification>) {
        pendingNotifications = notifications
    }

    override suspend fun getPendingNotifications(): List<Notification> {
        delay(getPendingNotificationTime)
        return pendingNotifications
    }

    override suspend fun markAsSent(notificationIds: List<String>) {
        delay(markAsSentTime)
        markedAsSent = notificationIds
    }
}

class FakeNotificationSender(
    private val successfulIds: Set<String> = emptySet(),
    private val sendingTime: Long = 0L
) : NotificationSender {
    var sentNotifications: List<Notification> = emptyList()

    override suspend fun send(notification: Notification): Boolean {
        delay(sendingTime)
        sentNotifications = sentNotifications + notification
        return if (successfulIds.isEmpty()) {
            true // By default, all sends are successful
        } else {
            notification.id in successfulIds
        }
    }
}
