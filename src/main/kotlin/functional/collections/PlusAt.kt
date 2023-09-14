package functional.collections

import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import kotlin.test.assertEquals

fun <T> List<T>.plusAt(index: Int, element: T): List<T> = TODO()

class PlusAtTest {

    @Test
    fun `Simple addition to the middle adds correctly at the position`() {
        assertEquals(listOf(1, 2, 7, 3), listOf(1, 2, 3).plusAt(2, 7))
        assertEquals(listOf("1", "2", "7", "3"), listOf("1", "2", "3").plusAt(2, "7"))
    }

    @Test
    fun `When we add at size position, element is added at the end`() {
        assertEquals(listOf(1, 2, 3, 7), listOf(1, 2, 3).plusAt(3, 7))
        assertEquals(listOf("1", "2", "3", "7"), listOf("1", "2", "3").plusAt(3, "7"))
    }

    @Test
    fun `When we add at 0, element is added at the beginning`() {
        assertEquals(listOf(7, 1, 2, 3), listOf(1, 2, 3).plusAt(0, 7))
        assertEquals(listOf("7", "1", "2", "3"), listOf("1", "2", "3").plusAt(0, "7"))
    }

    @Test
    fun `When we try to insert at illegal position, IllegalArgumentException error is thrown`() {
        assertTrue(catchError { listOf(1, 2, 3).plusAt(-1, 7) } is IllegalArgumentException)
        assertTrue(catchError { listOf(1, 2, 3).plusAt(8, 7) } is IllegalArgumentException)
        assertTrue(catchError { listOf(1, 2, 3).plusAt(10, 7) } is IllegalArgumentException)
        assertTrue(catchError { listOf(1, 2, 3).plusAt(100, 7) } is IllegalArgumentException)

    }

    private fun catchError(f: () -> Unit): Throwable? = try {
        f()
        null
    } catch (e: Throwable) {
        e
    }
}
