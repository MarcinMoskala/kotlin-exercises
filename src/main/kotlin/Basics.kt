import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals

// fizzBuzz function that returns String that represents what should be said in the FizzBuzz game for each number between 1 and 100.
// We list all this numbers in new lines, but we replace some of them with:
// “Fizz” if number is divisible by 3
// “Buzz” if number is divisible by 5
// “FizzBuzz” if number is divisible both by 3 and 5 (by 15)
// Print elements using `console.println`
fun fizzBuzz() {
    TODO()
}

// Fibonacci number that starts from 1 and 1 (fib(0) == 1, fib(1) == 1, fib(2) == 2, fib(3) == 3, fib(4) == 5, fib(5) == 8)
// https://en.wikipedia.org/wiki/Fibonacci_number
fun fib(n: Int): Int = TODO()

class BasicsTests {
    @Test
    fun testFizzBuzz() {
        val out = ByteArrayOutputStream()
        System.setOut(PrintStream(out))
        fizzBuzz()
        val expected = (1..100).joinToString("\n") { i ->
            when {
                i % 15 == 0 -> "FizzBuzz"
                i % 3 == 0 -> "Fizz"
                i % 5 == 0 -> "Buzz"
                else -> i.toString()
            }
        }
        assertEquals(expected, out.toString().trim())
    }

    @Test
    fun testFib() {
        assertEquals(1, fib(0))
        assertEquals(1, fib(1))
        assertEquals(2, fib(2))
        assertEquals(3, fib(3))
        assertEquals(5, fib(4))
        assertEquals(8, fib(5))
        assertEquals(13, fib(6))
        assertEquals(21, fib(7))
        assertEquals(34, fib(8))
        assertEquals(55, fib(9))
        assertEquals(89, fib(10))
    }
}
