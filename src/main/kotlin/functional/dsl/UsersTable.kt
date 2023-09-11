package functional.dsl

import org.junit.Test
import kotlin.test.assertEquals

data class User(val id: String, val name: String, val points: Int, val category: String)

fun userTable(users: List<User>): TableBuilder = TODO()

class UserTableTest {
    @Test
    fun `should work for empty list`() {
        // when
        val result = userTable(listOf())

        // then
        val expected = TableBuilder().apply {
            trs += TrBuilder().apply {
                tds += TdBuilder().apply { text = "Id" }
                tds += TdBuilder().apply { text = "Name" }
                tds += TdBuilder().apply { text = "Points" }
                tds += TdBuilder().apply { text = "Category" }
            }
        }
        assertEquals(expected, result)
    }

    @Test
    fun `should work for a list with a single element`() {
        // given
        val users = listOf(
            User("1", "Randy", 2, "A"),
        )

        // when
        val result = userTable(users)

        // then
        val expected = TableBuilder().apply {
            trs += TrBuilder().apply {
                tds += TdBuilder().apply { text = "Id" }
                tds += TdBuilder().apply { text = "Name" }
                tds += TdBuilder().apply { text = "Points" }
                tds += TdBuilder().apply { text = "Category" }
            }
            trs += TrBuilder().apply {
                tds += TdBuilder().apply { text = "1" }
                tds += TdBuilder().apply { text = "Randy" }
                tds += TdBuilder().apply { text = "2" }
                tds += TdBuilder().apply { text = "A" }
            }
        }
        assertEquals(expected, result)
    }

    @Test
    fun `should work for a list with multiple users`() {
        // given
        val users = listOf(
            User("1", "Randy", 2, "A"),
            User("4", "Andy", 4, "B"),
            User("3", "Mandy", 1, "C"),
            User("5", "Cindy", 5, "A"),
            User("2", "Lindy", 3, "B"),
        )

        // when
        val result = userTable(users)

        // then
        val expected = TableBuilder().apply {
            trs += TrBuilder().apply {
                tds += TdBuilder().apply { text = "Id" }
                tds += TdBuilder().apply { text = "Name" }
                tds += TdBuilder().apply { text = "Points" }
                tds += TdBuilder().apply { text = "Category" }
            }
            for (user in users) {
                trs += TrBuilder().apply {
                    tds += TdBuilder().apply { text = user.id }
                    tds += TdBuilder().apply { text = user.name }
                    tds += TdBuilder().apply { text = user.points.toString() }
                    tds += TdBuilder().apply { text = user.category }
                }
            }
        }
        assertEquals(expected, result)
    }
}
