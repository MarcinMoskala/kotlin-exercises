package essentials.loops.loops

import org.junit.Test
import kotlin.test.assertEquals

fun calculateSumOfSquares(n: Int): Int = TODO()

fun calculateSumOfEven(n: Int): Int = TODO()

fun countDownByStep(start: Int, end: Int, step: Int): String = TODO()

fun main() {
    // Examples for calculateSumOfSquares
    println(calculateSumOfSquares(0)) // 0
    println(calculateSumOfSquares(1)) // 1
    println(calculateSumOfSquares(2)) // 5 (1 + 4)
    println(calculateSumOfSquares(3)) // 14 (1 + 4 + 9)
    println(calculateSumOfSquares(4)) // 30 (1 + 4 + 9 + 16)

    // Example for calculateSumOfEven
    println(calculateSumOfEven(0)) // 0
    println(calculateSumOfEven(1)) // 0
    println(calculateSumOfEven(2)) // 2
    println(calculateSumOfEven(3)) // 2
    println(calculateSumOfEven(5)) // 6 (2 + 4)
    println(calculateSumOfEven(10))
    // 30 (2 + 4 + 6 + 8 + 10)
    println(calculateSumOfEven(12))
    // 42 (2 + 4 + 6 + 8 + 10 + 12)
    println(calculateSumOfEven(20))
    // 110 (2 + 4 + 6 + 8 + 10 + 12 + 14 + 16 + 18 + 20)

    // Example for countDownByStep
    println(countDownByStep(1, 1, 1)) // 1
    println(countDownByStep(5, 1, 2)) // 5, 3, 1
    println(countDownByStep(10, 1, 3)) // 10, 7, 4, 1
    println(countDownByStep(15, 5, 5)) // 15, 10, 5
    println(countDownByStep(20, 2, 3))
    // 20, 17, 14, 11, 8, 5, 2
    println(countDownByStep(10, 4, 3)) // 10, 7, 4
    println(countDownByStep(-1, -1, 1)) // -1
    println(countDownByStep(-5, -9, 2)) // -5, -7, -9
}

class LoopsTest {

    @Test
    fun testCalculateSumOfSquares() {
        assertEquals(1, calculateSumOfSquares(1))
        assertEquals(5, calculateSumOfSquares(2))
        assertEquals(14, calculateSumOfSquares(3))
        assertEquals(30, calculateSumOfSquares(4))
        assertEquals(385, calculateSumOfSquares(10))
        assertEquals(0, calculateSumOfSquares(0))
        assertEquals(0, calculateSumOfSquares(-1))
        assertEquals(0, calculateSumOfSquares(-1))
        assertEquals(0, calculateSumOfSquares(-3))
    }

    @Test
    fun testCalculateSumOfEven() {
        assertEquals(0, calculateSumOfEven(0))
        assertEquals(0, calculateSumOfEven(1))
        assertEquals(2, calculateSumOfEven(2))
        assertEquals(2, calculateSumOfEven(3))
        assertEquals(6, calculateSumOfEven(5))
        assertEquals(30, calculateSumOfEven(10))
        assertEquals(42, calculateSumOfEven(12))
        assertEquals(110, calculateSumOfEven(20))
        assertEquals(240, calculateSumOfEven(30))
        assertEquals(0, calculateSumOfEven(-1))
    }

    @Test
    fun testCountDownByStep() {
        assertEquals("1", countDownByStep(1, 1, 1))
        assertEquals("5, 3, 1", countDownByStep(5, 1, 2))
        assertEquals("10, 7, 4, 1", countDownByStep(10, 1, 3))
        assertEquals("15, 10, 5", countDownByStep(15, 5, 5))
        assertEquals("20, 17, 14, 11, 8, 5, 2", countDownByStep(20, 2, 3))
        assertEquals("10, 7, 4", countDownByStep(10, 4, 3))
        assertEquals("-1", countDownByStep(-1, -1, 1))
        assertEquals("-5, -7, -9", countDownByStep(-5, -9, 2))
        assertEquals("0", countDownByStep(0, 0, 1))
        assertEquals("0", countDownByStep(0, 0, 2))
    }
}
