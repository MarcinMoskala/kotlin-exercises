package coroutines.starting

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test

// In the Kafka message processor, the intention is to store the incoming record and commit
// the offset as quickly as possible, allowing engineService.notifyEngines(...) to run later
// in the background, so as not to delay processing under high load.
class RecordRepository(
    val handler: RecordHandler,
    val messageSender: MessageSender,
    val engineService: EngineService,
    val backgroundScope: CoroutineScope,
) {
    suspend fun processRecord(record: ConsumerRecord) {
        handler.storeEvent(record)
            .onSuccess { notifications ->
                backgroundScope.launch {
                    notifications.forEach {
                        engineService.notifyEngines(it)
                    }
                }
                messageSender.commit(record)
            }.onFailure {
                messageSender.commit(record)
            }
    }
}

class ProcessRecordTest {

    @Test
    fun `should commit record without waiting for notify engines`() = runTest {
        // Given
        val commitInvoked = CompletableDeferred<Unit>()
        val commitCompleted = AtomicBoolean(false)
        val notifyEngines = AtomicBoolean(false)

        val record = ConsumerRecord("test-topic")
        val handler = object : RecordHandler {
            override suspend fun storeEvent(record: ConsumerRecord): Result<List<Notification>> {
                return Result.success(listOf(Notification("1"), Notification("2")))
            }
        }
        val messageSender = object : MessageSender {
            override suspend fun commit(record: ConsumerRecord) {
                commitInvoked.complete(Unit)
                delay(2345)
                commitCompleted.set(true)
            }
        }
        val engineService = object : EngineService {
            override suspend fun notifyEngines(notification: Notification) {
                delay(1234)
                notifyEngines.set(true)
            }
        }
        val repository = RecordRepository(handler, messageSender, engineService, backgroundScope)

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

        // and the commit is invoked immediately
        assert(commitCompleted.get())
        assert(notifyEngines.get())
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
