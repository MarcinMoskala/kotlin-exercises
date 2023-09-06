package essentials

import org.junit.Test
import kotlin.test.assertEquals

fun secondsToPrettyTime(seconds: Int): String {
    return ""
}

class PrettyTimeTest {

    @Test
    fun testNegativeSeconds() {
        val seconds = -1
        val expected = "Invalid input"
        assertEquals(expected, secondsToPrettyTime(seconds))
    }

    @Test
    fun testOnlySeconds() {
        val seconds = 45
        val expected = "45 sec"
        assertEquals(expected, secondsToPrettyTime(seconds))
    }

    @Test
    fun testOnlyMinutes() {
        val seconds = 60
        val expected = "1 min"
        assertEquals(expected, secondsToPrettyTime(seconds))
    }

    @Test
    fun testMinutesAndSeconds() {
        val seconds = 150
        val expected = "2 min 30 sec"
        assertEquals(expected, secondsToPrettyTime(seconds))
    }

    @Test
    fun testMinutesAndSecondsWithRemainder() {
        val seconds = 1410
        val expected = "23 min 30 sec"
        assertEquals(expected, secondsToPrettyTime(seconds))
    }

    @Test
    fun testOnlyHours() {
        val seconds = 3600
        val expected = "1 h"
        assertEquals(expected, secondsToPrettyTime(seconds))
    }

    @Test
    fun testHoursMinutesAndSeconds() {
        val seconds = 3665
        val expected = "1 h 1 min 5 sec"
        assertEquals(expected, secondsToPrettyTime(seconds))
    }

    @Test
    fun testZeroSeconds() {
        val seconds = 0
        val expected = ""
        assertEquals(expected, secondsToPrettyTime(seconds))
    }

    @Test
    fun testHoursMinutesSecondsWithZeroMinutes() {
        val seconds = 3605
        val expected = "1 h 5 sec"
        assertEquals(expected, secondsToPrettyTime(seconds))
    }

    @Test
    fun testHoursMinutesWithZeroSeconds() {
        val seconds = 7200
        val expected = "2 h"
        assertEquals(expected, secondsToPrettyTime(seconds))
    }

    @Test
    fun testMinutesSecondsWithZeroHours() {
        val seconds = 150
        val expected = "2 min 30 sec"
        assertEquals(expected, secondsToPrettyTime(seconds))
    }

    @Test
    fun testLargeValue() {
        val seconds = 123456789
        val expected = "34293 h 33 min 9 sec"
        assertEquals(expected, secondsToPrettyTime(seconds))
    }
}
