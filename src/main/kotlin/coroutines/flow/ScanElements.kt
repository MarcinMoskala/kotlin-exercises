package flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

fun <T, R> Flow<T>.scanElements(initial: R, operation: suspend (accumulator: R, value: T) -> R): Flow<R> = TODO()

@Suppress("FunctionName")
class ScanElementsTests {

    @Test()
    fun scanElementsTests() = runTest {
        assertEquals(listOf(), emptyList<Int>().asFlow().scanElements(0) { acc, elem -> acc + elem }.toList())
        assertEquals(listOf(1), (1..1).asFlow().scanElements(0) { acc, elem -> acc + elem }.toList())
        assertEquals(listOf(1, 3, 6, 10, 15), (1..5).asFlow().scanElements(0) { acc, elem -> acc + elem }.toList())
        assertEquals(
            listOf("A", "AB", "ABC", "ABCD"),
            ('A'..'D').asFlow().scanElements("") { acc, elem -> acc + elem }.toList()
        )
    }
}