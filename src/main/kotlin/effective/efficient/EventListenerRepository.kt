package cheap

import cheap.EventListenerRepositoryTest.Event
import cheap.EventListenerRepositoryTest.Event.*
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals

class EventListenerRepository<E> {
    private var listeners = ConcurrentHashMap
        .newKeySet<EventListener<E>>()
    private val lock = Any()

    fun addEventListener(
        event: E,
        handler: () -> Unit
    ): EventListener<E> = synchronized(lock) {
        val listener = EventListener(event, handler)
        listeners += listener
        listener
    }

    fun invokeListeners(event: Event) {
        val eventListeners = listeners
            .filter { it.event == event }
        for (listener in eventListeners) {
            if (listener.isActive) {
                listener.handleEvent()
            } else {
                listeners.remove(listener)
            }
        }
    }
}

class EventListener<E>(
    val event: E,
    val handler: () -> Unit,
    isActive: Boolean = true
) {
    var isActive: Boolean = isActive
        private set

    fun handleEvent() {
        handler()
    }

    fun cancel() {
        isActive = false
    }
}

class EventListenerRepositoryTest {
    enum class Event { A, B, C }

    @Test
    fun `should invoke proper handlers`() {
        val eventListenerRepository = EventListenerRepository<Event>()
        var a = 0
        var b = 0
        var c = 0

        eventListenerRepository.addEventListener(A) { a++ }
        eventListenerRepository.addEventListener(B) { b++ }
        eventListenerRepository.addEventListener(C) { c++ }

        assertEquals(0, a)
        assertEquals(0, b)
        assertEquals(0, c)

        eventListenerRepository.invokeListeners(A)

        assertEquals(1, a)
        assertEquals(0, b)
        assertEquals(0, c)

        eventListenerRepository.invokeListeners(B)
        eventListenerRepository.invokeListeners(B)

        assertEquals(1, a)
        assertEquals(2, b)
        assertEquals(0, c)

        eventListenerRepository.invokeListeners(C)
        eventListenerRepository.invokeListeners(C)
        eventListenerRepository.invokeListeners(C)

        assertEquals(1, a)
        assertEquals(2, b)
        assertEquals(3, c)
    }

    @Test
    fun `should allow setting more than one handler for an event`() {
        val eventListenerRepository = EventListenerRepository<Event>()
        var a = 0
        var b = 0
        var c = 0

        eventListenerRepository.addEventListener(A) { a++ }
        eventListenerRepository.addEventListener(A) { b++ }
        eventListenerRepository.addEventListener(A) { c++ }

        eventListenerRepository.invokeListeners(A)

        assertEquals(1, a)
        assertEquals(1, b)
        assertEquals(1, c)
    }

    @Test
    fun `should allow listener cancelation`() {
        val eventListenerRepository = EventListenerRepository<Event>()
        var a = 0

        val listener = eventListenerRepository.addEventListener(A) { a++ }
        listener.cancel()

        eventListenerRepository.invokeListeners(A)

        assertEquals(0, a)
    }
}
