package essentials.functions

import org.junit.Test
import kotlin.test.assertEquals

fun Iterable<Number>.product(): Long = TODO()

class ProductTest {

    @Test
    fun `Product of empty IntRange is 1`() {
        @Suppress("EmptyRange")
        (assertEquals(1, (0..-1).product()))
    }

    @Test
    fun `Test products of different IntRanges`() {
        val rangeToProduct = mapOf(
                2..4 to 24L,
                1..4 to 24L,
                3..4 to 12L,
                100..100 to 100L
        )
        for ((range, product) in rangeToProduct)
            assertEquals(product, range.product())
    }

    @Test
    fun `Test products of different Int Collections`() {
        val collectionToProduct = mapOf(
                listOf(1,2,3) to 6L,
                listOf(2,3) to 6L,
                listOf(3) to 3L,
                listOf(10, 10, 10) to 1000L
        )
        for ((collection, product) in collectionToProduct)
            assertEquals(product, collection.product())
    }
}
