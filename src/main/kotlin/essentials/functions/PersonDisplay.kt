package essentials.functions

import org.junit.Test
import kotlin.test.assertEquals

fun formatPersonDisplay(
    name: String? = null,
    surname: String? = null,
    age: Int? = null,
): String {
    var result = ""
    if (name != null) {
        result += name
    }
    if (surname != null) {
        result += " $surname"
    }
    if (age != null) {
        result += " ($age)"
    }
    return result.trim()
}

fun main() {
    println(formatPersonDisplay("John", "Smith", 42))
    // John Smith (42)
    println(formatPersonDisplay("Alex", "Simonson"))
    // Alex Simonson
    println(formatPersonDisplay("Peter", age = 25))
    // Peter (25)
    println(formatPersonDisplay(surname="Johnson", age=18))
    // Johnson (18)
}

class PersonDisplayTest {
    @Test
    fun testFormatPersonDisplay() {
        val name = "John"
        val surname = "Smith"
        val age = 42
        val expected = "John Smith (42)"
        assertEquals(expected, formatPersonDisplay(name, surname, age))
    }

    @Test
    fun testFormatPersonDisplayWithoutAge() {
        val name = "Alex"
        val surname = "Simonson"
        val expected = "Alex Simonson"
        assertEquals(expected, formatPersonDisplay(name, surname))
    }

    @Test
    fun testFormatPersonDisplayWithoutSurname() {
        val name = "Peter"
        val age = 25
        val expected = "Peter (25)"
        assertEquals(expected, formatPersonDisplay(name = name, age = age))
    }

    @Test
    fun testFormatPersonDisplayWithoutName() {
        val surname = "Johnson"
        val age = 18
        val expected = "Johnson (18)"
        assertEquals(expected, formatPersonDisplay(surname = surname, age = age))
    }

    @Test
    fun testFormatPersonDisplayWithoutNameAndSurname() {
        val age = 18
        val expected = "(18)"
        assertEquals(expected, formatPersonDisplay(age = age))
    }

    @Test
    fun testFormatPersonDisplayWithoutParameters() {
        val expected = ""
        assertEquals(expected, formatPersonDisplay())
    }

    @Test
    fun testFormatPersonDisplayWithNullName() {
        val name: String? = null
        val surname = "Smith"
        val age = 42
        val expected = "Smith (42)"
        assertEquals(expected, formatPersonDisplay(name, surname, age))
    }

    @Test
    fun testFormatPersonDisplayWithNullSurname() {
        val name = "John"
        val surname: String? = null
        val age = 42
        val expected = "John (42)"
        assertEquals(expected, formatPersonDisplay(name, surname, age))
    }
}
