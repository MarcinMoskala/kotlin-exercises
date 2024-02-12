package coroutines.flow.mesageservice

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class MessageService(
    private val messageRepository: MessageRepository
) {
    fun threadsSearch(
        query: Flow<String>
    ): Flow<List<MessageThread>> = TODO()

    fun subscribeThreads(
        threads: Flow<MessageThread>
    ): Flow<MessageThreadUpdate> = TODO()

    fun sendMessages(
        messages: Flow<Message>
    ): Flow<MessageSendingResponse> = TODO()
}

interface MessageRepository {
    suspend fun searchThreads(
        query: String
    ): List<MessageThread>

    fun subscribeThread(
        threadId: String
    ): Flow<MessageThreadUpdate>
    
    suspend fun sendMessage(
        message: Message
    ): MessageSendingResponse
}

data class MessageThread(val id: String, val name: String)
data class MessageThreadUpdate(val threadId: String, val messages: List<Message>)
data class Message(val senderId: String, val text: String, val threadId: String)
data class MessageSendingResponse(val messageId: String, val success: Boolean)

class MessageServiceTests {
    @Test
    fun `should search for threads based on the last query`() = runTest {
        val repo = object : OpenMessageRepository() {
            override suspend fun searchThreads(query: String): List<MessageThread> {
                delay(1000)
                return listOf(MessageThread("Resp$query", "Name$query"))
            }
        }
        val service = MessageService(repo)
        val query = flow {
            emit("A")
            delay(500)
            emit("B")
            delay(1500)
            emit("C")
        }

        val result = service.threadsSearch(query)
            .withVirtualTime(this)
            .toList()

        assertEquals(
            listOf(
                ValueAndTime(listOf(MessageThread("RespB", "NameB")), 1500),
                ValueAndTime(listOf(MessageThread("RespC", "NameC")), 3000),
            ),
            result
        )
    }

    @Test
    fun `should subscribe to threads`() = runTest {
        val repo = object : OpenMessageRepository() {
            override fun subscribeThread(threadId: String): Flow<MessageThreadUpdate> = flow {
                emit(MessageThreadUpdate(threadId, listOf(Message("A", "B", threadId))))
                delay(1000)
                emit(MessageThreadUpdate(threadId, listOf(Message("C", "D", threadId))))
            }
        }
        val service = MessageService(repo)
        val threads = flow {
            emit(MessageThread("T1", "Name1"))
            delay(500)
            emit(MessageThread("T2", "Name2"))
            delay(1500)
            emit(MessageThread("T3", "Name3"))
        }

        val result = service.subscribeThreads(threads)
            .withVirtualTime(this)
            .toList()

        assertEquals(
            listOf(
                ValueAndTime(MessageThreadUpdate("T1", listOf(Message("A", "B", "T1"))), 0),
                ValueAndTime(MessageThreadUpdate("T2", listOf(Message("A", "B", "T2"))), 500),
                ValueAndTime(MessageThreadUpdate("T1", listOf(Message("C", "D", "T1"))), 1000),
                ValueAndTime(MessageThreadUpdate("T2", listOf(Message("C", "D", "T2"))), 1500),
                ValueAndTime(MessageThreadUpdate("T3", listOf(Message("A", "B", "T3"))), 2000),
                ValueAndTime(MessageThreadUpdate("T3", listOf(Message("C", "D", "T3"))), 3000),
            ),
            result
        )
    }

    @Test
    fun `should subscribe to unlimited number of hreads`() = runTest {
        val repo = object : OpenMessageRepository() {
            override fun subscribeThread(threadId: String): Flow<MessageThreadUpdate> = flow {
                emit(MessageThreadUpdate(threadId, listOf(Message("A", "B", threadId))))
                delay(1000)
                emit(MessageThreadUpdate(threadId, listOf(Message("C", "D", threadId))))
            }
        }
        val service = MessageService(repo)
        val threads = flow {
            repeat(1000) {
                emit(MessageThread("T$it", "Name$it"))
                delay(1)
            }
        }

        val result = service.subscribeThreads(threads).toList()

        assertEquals(2000, result.size)
        assertEquals(999 + 1000, currentTime)
    }

    @Test
    fun `should send messages synchroniously`() = runTest {
        val repo = object : OpenMessageRepository() {
            override suspend fun sendMessage(message: Message): MessageSendingResponse {
                delay(1000)
                return MessageSendingResponse(message.threadId, true)
            }
        }
        val service = MessageService(repo)
        val messages = channelFlow {
            send(Message("A", "B", "T1"))
            delay(500)
            send(Message("C", "D", "T2"))
            delay(1500)
            send(Message("E", "F", "T3"))
        }

        val result = service.sendMessages(messages)
            .withVirtualTime(this)
            .toList()

        assertEquals(
            listOf(
                ValueAndTime(MessageSendingResponse("T1", true), 1000),
                ValueAndTime(MessageSendingResponse("T2", true), 2000),
                ValueAndTime(MessageSendingResponse("T3", true), 3000),
            ),
            result
        )
    }
}

open class OpenMessageRepository : MessageRepository {
    override suspend fun searchThreads(query: String): List<MessageThread> {
        TODO()
    }

    override fun subscribeThread(threadId: String): Flow<MessageThreadUpdate> {
        TODO()
    }

    override suspend fun sendMessage(message: Message): MessageSendingResponse {
        TODO()
    }
}

fun <T> Flow<T>.withVirtualTime(testScope: TestScope): Flow<ValueAndTime<T>> =
    map { ValueAndTime(it, testScope.currentTime) }

data class ValueAndTime<T>(val value: T, val timeMillis: Long)
