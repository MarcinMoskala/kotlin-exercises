package essentials.functions

import org.junit.Test
import kotlin.test.assertEquals

// factorial(5) = 5! = 5 * 4 * 3 * 2 * 1 = 120
// https://en.wikipedia.org/wiki/Factorial
fun factorial(n: Int): Long = TODO()

class FactorialTest {

    @Test
    fun `Test factorial results`() {
        val numberWithFactorial = mapOf(
                0 to 1L,
                1 to 1L,
                2 to 2L,
                3 to 6L,
                4 to 24L,
                10 to 3628800L,
                15 to 1307674368000L,
                20 to 2432902008176640000L
        )
        for ((i, factorialResult) in numberWithFactorial) {
            assertEquals(factorialResult, factorial(i))
        }
    }
}
