package essentials

import org.junit.Test
import kotlin.test.assertEquals

//fun main() {
//    val laptop = Product("Laptop", 999.99, 5)
//
//    println(laptop.name) // Laptop
//    println(laptop.quantity) // 5
//    println(laptop.calculateTotalValue()) // 4999.95
//
//    laptop.restock(3)
//    
//    println(laptop.quantity) // 8
//    println(laptop.calculateTotalValue()) // 7999.92
//    
//    laptop.quantity = -2
//    
//    println(laptop.quantity) // 0
//    println(laptop.calculateTotalValue()) // 0.0
//    
//    laptop.quantity = 10
//    
//    println(laptop.quantity) // 10
//    println(laptop.calculateTotalValue()) // 9999.9
//}

class ProductTest {

//    @Test
//    fun `initial quantity should be set correctly`() {
//        val product = Product("Apple", 1.0, 10)
//        assertEquals(10, product.quantity)
//    }
//
//    @Test
//    fun `set positive quantity should work`() {
//        val product = Product("Apple", 1.0, 10)
//        product.quantity = 5
//        assertEquals(5, product.quantity)
//    }
//
//    @Test
//    fun `set negative quantity should be treated as zero`() {
//        val product = Product("Apple", 1.0, 10)
//        product.quantity = -2
//        assertEquals(0, product.quantity)
//    }
//
//    @Test
//    fun `calculate total value should work`() {
//        val product = Product("Apple", 1.0, 10)
//        assertEquals(10.0, product.calculateTotalValue())
//    }
//
//    @Test
//    fun `restock should add to quantity`() {
//        val product = Product("Apple", 1.0, 10)
//        product.restock(5)
//        assertEquals(15, product.quantity)
//    }
//
//    @Test
//    fun `restock with negative quantity should not change quantity`() {
//        val product = Product("Apple", 1.0, 10)
//        product.restock(-2)
//        assertEquals(10, product.quantity)
//    }
//
//    @Test
//    fun `calculate total value with zero quantity should be zero`() {
//        val product = Product("Apple", 1.0, 0)
//        assertEquals(0.0, product.calculateTotalValue())
//    }
//
//    @Test
//    fun `restock with zero quantity change should not change quantity`() {
//        val product = Product("Apple", 1.0, 10)
//        product.restock(0)
//        assertEquals(10, product.quantity)
//    }
//
//    @Test
//    fun `restock with multiple additions should update quantity`() {
//        val product = Product("Apple", 1.0, 10)
//        product.restock(5)
//        product.restock(3)
//        assertEquals(18, product.quantity)
//    }
//
//    @Test
//    fun `restock with negative addition after positive addition should not update quantity`() {
//        val product = Product("Apple", 1.0, 10)
//        product.restock(5)
//        product.restock(-3)
//        assertEquals(15, product.quantity)
//    }
}
