package functional.dsl.table

import org.junit.Test
import kotlin.test.assertEquals

//fun createTable(): TableBuilder = table {
//    tr {
//        td { +"A" }
//        td { +"B" }
//    }
//    tr {
//        td { +"C" }
//        td { +"D" }
//    }
//}

data class TableBuilder(
    var trs: List<TrBuilder> = emptyList()
) {
    override fun toString(): String =
        "<table>${trs.joinToString(separator = "")}</table>"
}
data class TrBuilder(
    var tds: List<TdBuilder> = emptyList()
) {
    override fun toString(): String =
        "<tr>${tds.joinToString(separator = "")}</tr>"
}
data class TdBuilder(
    var text: String = ""
) {
    override fun toString(): String = "<td>$text</td>"
}

fun main() {
    // println(createTable()) //<table><tr><td>This is row 1</td><td>This is row 2</td></tr></table>
}

class HtmlDslTest {
//    @Test
//    fun createTableTest() {
//        val html = TableBuilder().apply {
//            trs += TrBuilder().apply {
//                tds += TdBuilder().apply { text = "A" }
//                tds += TdBuilder().apply { text = "B" }
//            }
//            trs += TrBuilder().apply {
//                tds += TdBuilder().apply { text = "C" }
//                tds += TdBuilder().apply { text = "D" }
//            }
//        }
//        assertEquals(html, createTable())
//    }
//
//    @Test
//    fun `Table can be created and it is empty by default`() {
//        val expected = TableBuilder()
//        val actual =  table {}
//        assertEquals(expected, actual)
//    }
//
//    @Test
//    fun `Tr can be created and it is empty`() {
//        val expected = TableBuilder().apply {
//            trs += TrBuilder()
//        }
//        val actual =  table {
//            tr {}
//        }
//        assertEquals(expected, actual)
//    }
//
//    @Test
//    fun `Multiple tr can be created`() {
//        val expected = TableBuilder().apply {
//            trs += TrBuilder()
//            trs += TrBuilder()
//        }
//        val actual =  table {
//            tr {}
//            tr {}
//        }
//        assertEquals(expected, actual)
//    }
//
//    @Test
//    fun `Td can be created and it is empty`() {
//        val expected = TableBuilder().apply {
//            trs += TrBuilder().apply {
//                tds += TdBuilder()
//            }
//        }
//        val actual =  table {
//            tr {
//                td {}
//            }
//        }
//        assertEquals(expected, actual)
//    }
//
//    @Test
//    fun dslTestStandard() {
//        val expected = TableBuilder().apply {
//            trs += TrBuilder().apply {
//                tds += TdBuilder().apply { text = "A" }
//                tds += TdBuilder().apply { text = "B" }
//            }
//            trs += TrBuilder().apply {
//                tds += TdBuilder().apply { text = "C" }
//                tds += TdBuilder().apply { text = "D" }
//            }
//        }
//        val actual =  table {
//            tr {
//                td { +"A" }
//                td { +"B" }
//            }
//            tr {
//                td { +"C" }
//                td { +"D" }
//            }
//        }
//        assertEquals(expected, actual)
//    }
//
//    @Test
//    fun dslTestMoreColons() {
//        val expected = TableBuilder().apply {
//            trs += TrBuilder().apply {
//                tds += TdBuilder().apply { text = "A" }
//                tds += TdBuilder().apply { text = "B" }
//                tds += TdBuilder().apply { text = "C" }
//            }
//            trs += TrBuilder().apply {
//                tds += TdBuilder().apply { text = "C" }
//                tds += TdBuilder().apply { text = "D" }
//            }
//        }
//        val actual =  table {
//            tr {
//                td { +"A" }
//                td { +"B" }
//                td { +"C" }
//            }
//            tr {
//                td { +"C" }
//                td { +"D" }
//            }
//        }
//        assertEquals(expected, actual)
//    }
}
