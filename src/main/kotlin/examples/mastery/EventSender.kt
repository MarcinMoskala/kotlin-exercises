package examples.mastery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class EventSender(
    var sendEvents: suspend (List<Event>) -> Unit,
) {
    private val waiting = mutableSetOf<Event>()
    private val lock = Any()

    fun schedule(event: Event) = synchronized(lock) {
        waiting.add(event)
    }

    suspend fun sendBundle() {
        sendEvents(waiting.toList())
        waiting.clear()
    }
}

data class Event(val name: String)

suspend fun main() {
    val eventsSent = ConcurrentHashMap.newKeySet<Event>()
    val eventSender = EventSender { events ->
        eventsSent.addAll(events)
        delay(1)
    }
    withContext(Dispatchers.Default) {
        repeat(1000) { i ->
            launch {
                repeat(1000) { j ->
                    eventSender.schedule(Event("Event${i}_$j"))
                }
            }
        }
        launch {
            repeat(1000) {
                eventSender.sendBundle()
            }
        }
    }
    eventSender.sendBundle()
    println("Sent events: ${eventsSent.size}")
}