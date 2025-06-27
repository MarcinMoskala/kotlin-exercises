package coroutines.starting.recordrepository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Test

class RecordRepository(
    val handler: RecordHandler,
    val messageSender: MessageSender,
    val engineService: EngineService,
    val backgroundScope: CoroutineScope,
) {
    suspend fun processRecord(record: ConsumerRecord) {
        handler.storeEvent(record)
            .onSuccess { notifications ->
                coroutineScope {
                    launch {
                        notifications.forEach {
                            engineService.notifyEngines(it)
                        }
                    }
                }
            }.also {
                messageSender.commit(record)
            }
    }
}

data class ConsumerRecord(
    val topic: String,
)

interface RecordHandler {
    suspend fun storeEvent(record: ConsumerRecord): Result<List<Notification>>
}

interface MessageSender {
    suspend fun commit(record: ConsumerRecord)
}

interface EngineService {
    suspend fun notifyEngines(notification: Notification)
}

data class Notification(
    val id: String,
)

class ProcessRecordTest {

    @Test
    fun `should commit record without waiting for notify engines`() = runTest {
        // Given
        val commitInvoked = CompletableDeferred<Unit>()
        val commitCompleted = AtomicBoolean(false)
        val notifyEngines = AtomicBoolean(false)
        var notifyEnginesCallCount = 0
        var notifyEnginesStarted = false

        val record = ConsumerRecord("test-topic")
        val handler = object : RecordHandler {
            override suspend fun storeEvent(record: ConsumerRecord): Result<List<Notification>> {
                return Result.success(listOf(Notification("1"), Notification("2")))
            }
        }
        val messageSender = object : MessageSender {
            override suspend fun commit(record: ConsumerRecord) {
                // If notifyEngines has started before commit is called,
                // it means commit was incorrectly called after launching background tasks
                if (notifyEnginesStarted) {
                    assert(false) { "messageSender.commit was called after notifyEngines started, suggesting it was called in a background scope" }
                }
                commitInvoked.complete(Unit)
                delay(2345)
                commitCompleted.set(true)
            }
        }
        val engineService = object : EngineService {
            override suspend fun notifyEngines(notification: Notification) {
                notifyEnginesStarted = true
                notifyEnginesCallCount++
                delay(1234)
                notifyEngines.set(true)
            }
        }
        val repository = RecordRepository(handler, messageSender, engineService, this)

        // when
        launch {
            repository.processRecord(record)
        }

        // then the commit is invoked immediately
        commitInvoked.await()
        assert(currentTime == 0L)

        // then the overall time is the bigger of the two delays
        advanceUntilIdle()
        assert(currentTime == 2345L)

        // and the commit is completed
        assert(commitCompleted.get())
        // and notify engines is called for each notification
        assert(notifyEngines.get())
        assert(notifyEnginesCallCount == 2) { "Expected 2 notifications to be processed, but got $notifyEnginesCallCount" }
    }

    @Test
    fun `should commit record when store event fails`() = runTest {
        // Given
        val commitInvoked = CompletableDeferred<Unit>()
        val commitCompleted = AtomicBoolean(false)
        var notifyEnginesCallCount = 0

        val record = ConsumerRecord("test-topic")
        val handler = object : RecordHandler {
            override suspend fun storeEvent(record: ConsumerRecord): Result<List<Notification>> {
                return Result.failure(RuntimeException("Test exception"))
            }
        }
        val messageSender = object : MessageSender {
            override suspend fun commit(record: ConsumerRecord) {
                commitInvoked.complete(Unit)
                delay(1000)
                commitCompleted.set(true)
            }
        }
        val engineService = object : EngineService {
            override suspend fun notifyEngines(notification: Notification) {
                notifyEnginesCallCount++
                delay(500)
            }
        }
        val repository = RecordRepository(handler, messageSender, engineService, this)

        // when
        launch {
            repository.processRecord(record)
        }

        // then the commit is invoked immediately
        commitInvoked.await()
        assert(currentTime == 0L)

        // then we complete all coroutines
        advanceUntilIdle()
        assert(currentTime == 1000L)

        // and the commit is completed
        assert(commitCompleted.get())
        // and notify engines is not called
        assert(notifyEnginesCallCount == 0) { "Expected 0 notifications to be processed, but got $notifyEnginesCallCount" }
    }

    @Test
    fun `should launch background tasks for each notification`() = runTest {
        // Given
        val notifications = listOf(
            Notification("1"),
            Notification("2"),
            Notification("3")
        )
        val notifiedEngines = mutableListOf<String>()
        val notificationProcessingTimes = mutableMapOf<String, Long>()
        val processingStartTimes = mutableMapOf<String, Long>()

        val record = ConsumerRecord("test-topic")
        val handler = object : RecordHandler {
            override suspend fun storeEvent(record: ConsumerRecord): Result<List<Notification>> {
                return Result.success(notifications)
            }
        }
        val messageSender = object : MessageSender {
            override suspend fun commit(record: ConsumerRecord) {
                // Do nothing
            }
        }
        val engineService = object : EngineService {
            override suspend fun notifyEngines(notification: Notification) {
                // Record the start time for this notification
                processingStartTimes[notification.id] = currentTime

                // Add different delays for each notification to verify they run in parallel
                when (notification.id) {
                    "1" -> delay(100)
                    "2" -> delay(200)
                    "3" -> delay(300)
                }

                // Record the processing time (how long it took to process this notification)
                notificationProcessingTimes[notification.id] = currentTime - processingStartTimes[notification.id]!!
                notifiedEngines.add(notification.id)
            }
        }
        val repository = RecordRepository(handler, messageSender, engineService, this)

        // when
        repository.processRecord(record)

        // then
        advanceUntilIdle()

        // Verify all notifications were processed
        assert(notifiedEngines.size == 3) { "Expected 3 notifications to be processed, but got ${notifiedEngines.size}" }
        assert(notifiedEngines.containsAll(listOf("1", "2", "3"))) { "Not all notifications were processed: $notifiedEngines" }

        // Verify that the total time is equal to the longest notification processing time
        // This verifies that notifications were processed in parallel, not sequentially
        val maxProcessingTime = notificationProcessingTimes.values.maxOrNull() ?: 0
        assert(currentTime == maxProcessingTime) {
            "Expected total time to be $maxProcessingTime (longest notification processing time), but was $currentTime. " +
            "This suggests notifications were not processed in parallel."
        }

        // Verify that each notification took the expected amount of time to process
        assert(notificationProcessingTimes["1"] == 100L) { "Notification 1 should take 100ms, but took ${notificationProcessingTimes["1"]}ms" }
        assert(notificationProcessingTimes["2"] == 200L) { "Notification 2 should take 200ms, but took ${notificationProcessingTimes["2"]}ms" }
        assert(notificationProcessingTimes["3"] == 300L) { "Notification 3 should take 300ms, but took ${notificationProcessingTimes["3"]}ms" }
    }
}
