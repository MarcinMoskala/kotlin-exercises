package essentials.e18

import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class Inventory {
    private val products = mutableListOf<Product>()
    private val productIdToProducer =
        mutableMapOf<String, String>()
    private val sellers = mutableSetOf<String>()

    fun addProduct(product: Product, producer: String) {
        products.add(product)
        productIdToProducer[product.id] = producer
    }

    fun removeProduct(product: Product) {
        products.remove(product)
        productIdToProducer.remove(product.id)
    }

    fun addSeller(seller: String) {
        sellers.add(seller)
    }

    fun removeSeller(seller: String) {
        sellers.remove(seller)
    }

    fun getProductsCount() = products.size

    fun hasProduct(product: Product) =
        products.contains(product)

    fun hasProducts() = products.isNotEmpty()

    fun getProducer(product: Product) =
        productIdToProducer[product.id]

    fun produceInventoryDisplay(): String {
        var result = "Inventory:\n"
        for (product in products) {
            val name = product.name
            val category = product.category
            val price = product.price
            result += "$name ($category) - $price\n"
            val producer = productIdToProducer[product.id]
            result += "Produced by: $producer\n"
        }
        result += "Sellers: $sellers"
        return result
    }
}

class Product(
    val id: String,
    val name: String,
    val price: Double,
    val category: String,
)

fun main() {
    val inventory = Inventory()
    println(inventory.hasProducts()) // false

    val p1 = Product("P1", "Phone", 599.99, "Electronics")
    val p2 = Product("P2", "Laptop", 1199.99, "Electronics")
    val p3 = Product("P3", "Shirt", 29.99, "Clothing")

    inventory.addProduct(p1, "TechCompany")
    inventory.addProduct(p2, "TechCompany")
    inventory.addProduct(p3, "ClothingCompany")

    inventory.addSeller("Seller1")
    inventory.addSeller("Seller2")

    println(inventory.getProductsCount()) // 3
    println(inventory.hasProduct(p1)) // true
    println(inventory.hasProducts()) // true
    println(inventory.getProducer(p1)) // TechCompany

    println(inventory.produceInventoryDisplay())
    // Inventory:
    // Phone (Electronics) - $599.99
    // Produced by: TechCompany
    // Laptop (Electronics) - $1199.99
    // Produced by: TechCompany
    // Shirt (Clothing) - $29.99
    // Produced by: ClothingCompany
    // Sellers: [Seller1, Seller2]

    inventory.removeProduct(p2)
    inventory.addSeller("Seller1")
    inventory.removeSeller("Seller2")

    println(inventory.getProductsCount()) // 2
    println(inventory.hasProduct(p1)) // true
    println(inventory.hasProduct(p2)) // false
    println(inventory.hasProducts()) // true
    println(inventory.getProducer(p2)) // null

    println(inventory.produceInventoryDisplay())
    // Inventory:
    // Phone (Electronics) - $599.99
    // Produced by: TechCompany
    // Shirt (Clothing) - $29.99
    // Produced by: ClothingCompany
    // Sellers: [Seller1]
}

class InventoryTest {

    private lateinit var inventory: Inventory
    private val apple = Product("1", "Apple", 0.5, "Fruit")
    private var banana = Product("2", "Banana", 0.3, "Fruit")

    @Before
    fun setup() {
        inventory = Inventory()
    }

    @Test
    fun `addProduct should increase product count and set producer`() {
        inventory.addProduct(apple, "FruitCorp")

        assertEquals(1, inventory.getProductsCount())
        assertTrue(inventory.hasProduct(apple))
        assertEquals("FruitCorp", inventory.getProducer(apple))
    }

    @Test
    fun `removeProduct should decrease product count and remove producer`() {
        inventory.addProduct(apple, "FruitCorp")
        inventory.removeProduct(apple)

        assertEquals(0, inventory.getProductsCount())
        assertFalse(inventory.hasProduct(apple))
        assertNull(inventory.getProducer(apple))
    }

    @Test
    fun `addSeller should add seller to the inventory`() {
        inventory.addSeller("SellerA")

        assertTrue(inventory.produceInventoryDisplay().contains("SellerA"))
    }

    @Test
    fun `removeSeller should remove seller from the inventory`() {
        inventory.addSeller("SellerA")
        inventory.removeSeller("SellerA")

        assertFalse(inventory.produceInventoryDisplay().contains("SellerA"))
    }

    @Test
    fun `produceInventoryDisplay should display products and sellers correctly`() {
        inventory.addProduct(apple, "FruitCorp")
        inventory.addProduct(banana, "TropicalFruitCorp")
        inventory.addSeller("SellerA")
        inventory.addSeller("SellerB")

        val expectedDisplay = """
            Inventory:
            Apple (Fruit) - 0.5
            Produced by: FruitCorp
            Banana (Fruit) - 0.3
            Produced by: TropicalFruitCorp
            Sellers: [SellerA, SellerB]
        """.trimIndent()

        assertEquals(expectedDisplay, inventory.produceInventoryDisplay())
    }

    @Test
    fun `hasProducts should return true when there are products`() {
        inventory.addProduct(apple, "FruitCorp")

        assertTrue(inventory.hasProducts())
    }

    @Test
    fun `hasProducts should return false when there are no products`() {
        assertFalse(inventory.hasProducts())
    }

    @Test
    fun `getProducer should return null for nonexistent product`() {
        assertNull(inventory.getProducer(apple))
    }
}
