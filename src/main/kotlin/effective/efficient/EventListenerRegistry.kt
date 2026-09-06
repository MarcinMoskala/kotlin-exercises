package effective.efficient.eventlistenerregistry

import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals
import java.lang.ref.WeakReference

class EventListenerRegistry<E> {
    private val listeners = ConcurrentHashMap
        .newKeySet<EventListener<E>>()

    fun addEventListener(
        event: E,
        handler: () -> Unit
    ): EventListener<E> {
        val listener = EventListener(event, handler)
        listeners += listener
        return listener
    }

    fun invokeListeners(event: E) {
        listeners
            .filter { it.event == event && it.isActive }
            .forEach { it.handleEvent() }
    }
}

class EventListener<E>(
    val event: E,
    private val handler: () -> Unit,
) {
    var isActive: Boolean = true
        private set

    fun handleEvent() {
        handler()
    }

    fun cancel() {
        isActive = false
    }
}

enum class Event { A, B, C }

class EventListenerRegistryTest {
    @Test
    fun `should invoke proper handlers`() {
        val eventListenerRepository = EventListenerRegistry<Event>()
        var a = 0
        var b = 0
        var c = 0

        eventListenerRepository.addEventListener(Event.A) { a++ }
        eventListenerRepository.addEventListener(Event.B) { b++ }
        eventListenerRepository.addEventListener(Event.C) { c++ }

        assertEquals(0, a)
        assertEquals(0, b)
        assertEquals(0, c)

        eventListenerRepository.invokeListeners(Event.A)

        assertEquals(1, a)
        assertEquals(0, b)
        assertEquals(0, c)

        eventListenerRepository.invokeListeners(Event.B)
        eventListenerRepository.invokeListeners(Event.B)

        assertEquals(1, a)
        assertEquals(2, b)
        assertEquals(0, c)

        eventListenerRepository.invokeListeners(Event.C)
        eventListenerRepository.invokeListeners(Event.C)
        eventListenerRepository.invokeListeners(Event.C)

        assertEquals(1, a)
        assertEquals(2, b)
        assertEquals(3, c)
    }

    @Test
    fun `should allow setting more than one handler for an event`() {
        val eventListenerRepository = EventListenerRegistry<Event>()
        var a = 0
        var b = 0
        var c = 0

        eventListenerRepository.addEventListener(Event.A) { a++ }
        eventListenerRepository.addEventListener(Event.A) { b++ }
        eventListenerRepository.addEventListener(Event.A) { c++ }

        eventListenerRepository.invokeListeners(Event.A)

        assertEquals(1, a)
        assertEquals(1, b)
        assertEquals(1, c)
    }

    @Test
    fun `should allow listener cancelation`() {
        val eventListenerRepository = EventListenerRegistry<Event>()
        var a = 0

        val listener = eventListenerRepository.addEventListener(Event.A) { a++ }
        listener.cancel()

        eventListenerRepository.invokeListeners(Event.A)

        assertEquals(0, a)
    }

    @Test
    fun `should clean memory after cancel`() {
        val eventListenerRepository = EventListenerRegistry<Event>()
        var ref: WeakReference<Any>? = null

        run {
            val someMemory = Any()
            val listener = eventListenerRepository.addEventListener(Event.A) {
                println(someMemory)
                ref = WeakReference(someMemory)
            }
            listener.handleEvent()
            listener.cancel()
        }

        repeat(100) {
            System.gc()
        }

        assert(ref != null)
        assert(ref?.get() == null)
    }

    @Test
    fun `should clean canceled event listener when invokeListeners`() {
        val eventListenerRepository = EventListenerRegistry<Event>()
        var ref: WeakReference<EventListener<Event>>? = null
        run {
            val event = eventListenerRepository.addEventListener(Event.A) {}
            ref = WeakReference(event)
            event.cancel()
            eventListenerRepository.invokeListeners(Event.A)
        }
        
        repeat(100) {
            System.gc()
        }

        assert(ref != null)
        assert(ref?.get() == null)
    }
}
