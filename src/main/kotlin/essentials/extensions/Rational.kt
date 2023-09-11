package essentials.extensions

import org.junit.Test
import kotlin.test.assertEquals

data class Rational(val numerator: Int, val denominator: Int)

fun Int.r(): Rational = TODO()
fun Pair<Int, Int>.r(): Rational = TODO()

fun main(args: Array<String>) {
    print(1.r())
    print((1 to 2).r())
}

class RationalTest {
    @Test
    fun testIntExtension() {
        assertEquals(Rational(4, 1), 4.r())
        for (i in 1..100) {
            assertEquals(Rational(i, 1), i.r())
        }
    }

    @Test
    fun testPairExtension() {
        assertEquals(Rational(2, 3), Pair(2, 3).r())

        for (l in 1..10) {
            for (r in 1..10) {
                val p = Pair(l, r)
                assertEquals(Rational(l, r), p.r())
            }
        }
    }
}
