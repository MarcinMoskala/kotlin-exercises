import org.junit.Test
import kotlin.test.assertEquals

val fibonacci = sequence<Int> {
    TODO()
}

@Suppress("FunctionName")
internal class FibonacciTests {

    @Test
    fun `First two numbers should be 1 and 1`() {
        assertEquals(listOf(1, 1), fibonacci.take(2).toList())
    }

    @Test
    fun `Check first 11 numbers`() {
        assertEquals(listOf(1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89), fibonacci.take(11).toList())
    }
}
