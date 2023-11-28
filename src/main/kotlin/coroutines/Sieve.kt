import org.junit.Test
import kotlin.test.assertEquals

val primes = sequence<Int> {
    TODO()
}

// TODO: Delete it and replace it with sequence builder (above)
fun getPrimeNumbers(num: Int): List<Int> {
    var numbers = generateSequence(2) { it + 1 }
    val primes = mutableListOf<Int>()
    repeat(num) {
        val prime = numbers.first()
        primes += prime
        numbers = numbers.drop(1).filter { it % prime != 0 }
    }
    return primes
}

fun main() {
    print(getPrimeNumbers(20))
    // print(primes.take(20).toList())
}

@Suppress("FunctionName")
class SieveTests {

    @Test
    fun `First prime number is 2`() {
        assert(2 in primes)
        assertEquals(0, primes.indexOf(2))
        assertEquals(2, primes.first())
    }

    @Test
    fun `Second prime number is 3`() {
        assert(3 in primes)
        assertEquals(1, primes.indexOf(3))
        assertEquals(3, primes.take(2).last())
    }

    @Test
    fun `Third prime number is 5`() {
        assert(5 in primes)
        assertEquals(2, primes.indexOf(5))
        assertEquals(5, primes.take(3).last())
    }

    @Test
    fun `Check first 10 prime numbers`() {
        assertEquals(listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29), primes.take(10).toList())
    }

    @Test
    fun `Check first 100 prime numbers`() {
        val first100primes = listOf(
            2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101,
            103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199,
            211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307, 311, 313, 317,
            331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439, 443,
            449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541
        )
        assertEquals(first100primes, primes.take(100).toList())
    }

    @Test
    fun `Prime at 200th position is 1223`() {
        assertEquals(1223, primes.take(200).last())
    }
}
