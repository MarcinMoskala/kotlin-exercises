package functional.collections

import org.junit.Test
import kotlin.test.assertEquals

data class Shop(val name: String, val customers: List<Customer>)
data class Customer(val name: String, val city: City, val orders: List<Order>)
data class Order(val products: List<Product>, val isDelivered: Boolean)
data class Product(val name: String, val price: Double)
data class City(val name: String)

// Get customers with undelivered products
fun Shop.getWaitingCustomers(): List<Customer> = TODO()

// Return the number of times the given product was ordered.
// Note: a customer may order the same product for several times.
fun Shop.countProductSales(product: Product): Int = TODO()

// Get customers who paid at least `amount`
fun Shop.getCustomers(minAmount: Double): List<Customer> = TODO()

class ShopTests {
    @Test
    fun testCustomersWhoPaidAtLeastFilter() {
        val customersWhoBoughtAtLeast10 = shop.customers - customersMap[cooper]
        assertEquals(customersWhoBoughtAtLeast10, shop.getCustomers(10.0))
        val customersWhoBoughtAtLeast150 = customersWhoBoughtAtLeast10 - customersMap[nathan] - customersMap[bajram]
        assertEquals(customersWhoBoughtAtLeast150, shop.getCustomers(150.0))
        val customersWhoBoughtAtLeast300 = listOf(customersMap[lucas], customersMap[reka])
        assertEquals(customersWhoBoughtAtLeast300, shop.getCustomers(300.0))
    }

    @Test
    fun testCustomersWithUndeliveredProductsilter() {
        assertEquals(listOf(), Shop("", listOf()).getWaitingCustomers())
        assertEquals(listOf(customersMap[reka]), shop.getWaitingCustomers())

        val fakeProducts = listOf(Product("", 0.0))
        val emptyCity = City("")
        val c1 = Customer("C1", emptyCity, listOf(Order(fakeProducts, isDelivered = false)))
        val c2 = Customer("C2", emptyCity, listOf(Order(fakeProducts, isDelivered = false)))
        val cDelivered = Customer("CDelivered", emptyCity, listOf(Order(fakeProducts, isDelivered = true)))
        assertEquals(listOf(c1), Shop("", listOf(c1)).getWaitingCustomers())
        assertEquals(listOf(c2), Shop("", listOf(c2)).getWaitingCustomers())
        assertEquals(listOf(), Shop("", listOf(cDelivered)).getWaitingCustomers())
        assertEquals(listOf(c1, c2), Shop("", listOf(c1, c2)).getWaitingCustomers())
        assertEquals(listOf(c1, c2), Shop("", listOf(c1, cDelivered, c2)).getWaitingCustomers())
    }

    @Test
    fun testNumberOfTimesEachProductWasOrdered() {
        assertEquals(4, shop.countProductSales(idea))
    }

    @Test
    fun testNumberOfTimesEachProductWasOrderedForRepeatedProduct() {
        assertEquals(3, shop.countProductSales(reSharper), "A customer may order a product for several times")
    }

    @Test
    fun testNumberOfTimesEachProductWasOrderedForRepeatedInOrderProduct() {
        assertEquals(3, shop.countProductSales(phpStorm), "An order may contain a particular product more than once")
    }
}

private val idea = Product("IntelliJ IDEA Ultimate", 199.0)
private val reSharper = Product("ReSharper", 149.0)
private val dotTrace = Product("DotTrace", 159.0)
private val dotMemory = Product("DotMemory", 129.0)
private val phpStorm = Product("PhpStorm", 99.0)
private val rubyMine = Product("RubyMine", 99.0)
private val webStorm = Product("WebStorm", 49.0)

//customers
private const val lucas = "Lucas"
private const val cooper = "Cooper"
private const val nathan = "Nathan"
private const val reka = "Reka"
private const val bajram = "Bajram"
private const val asuka = "Asuka"
private const val riku = "Riku"

//cities
private val Canberra = City("Canberra")
private val Vancouver = City("Vancouver")
private val Budapest = City("Budapest")
private val Ankara = City("Ankara")
private val Tokyo = City("Tokyo")

private fun customer(name: String, city: City, vararg orders: Order) = Customer(name, city, orders.toList())
private fun order(vararg products: Product, isDelivered: Boolean = true) = Order(products.toList(), isDelivered)
private fun shop(name: String, vararg customers: Customer) = Shop(name, customers.toList())

private val shop = shop("jb test shop",
    customer(lucas, Canberra,
        order(reSharper),
        order(reSharper, dotMemory, dotTrace)
    ),
    customer(cooper, Canberra),
    customer(nathan, Vancouver,
        order(rubyMine, webStorm)
    ),
    customer(reka, Budapest,
        order(idea, isDelivered = false),
        order(idea, isDelivered = false),
        order(idea)
    ),
    customer(bajram, Ankara,
        order(reSharper)
    ),
    customer(asuka, Tokyo,
        order(idea)
    ),
    customer(riku, Tokyo,
        order(phpStorm, phpStorm),
        order(phpStorm)
    )
)

private val customersMap = shop.customers.associateBy { it.name }
