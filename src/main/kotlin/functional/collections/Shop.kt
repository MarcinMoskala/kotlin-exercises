package functional.collections.shop

import org.junit.Test
import kotlin.test.assertEquals

data class Shop(val name: String, val customers: List<Customer>)
data class Customer(val name: String, val city: City, val orders: List<Order>)
data class Order(val products: List<Product>, val isDelivered: Boolean)
data class Product(val name: String, val price: Double)
data class City(val name: String)

fun Shop.getWaitingCustomers(): List<Customer> = TODO()

fun Shop.countProductSales(product: Product): Int = TODO()

fun Shop.getCustomers(minAmount: Double): List<Customer> = TODO()

class ShopFunctionsTests {
    @Test
    fun `getWaitingCustomers should get customers with any delivered order`() {
        // given
        val shop = Shop(
            name = "Test shop",
            customers = listOf(
                Customer(
                    name = "Customer 1",
                    city = City("City 1"),
                    orders = listOf(
                        Order(
                            products = listOf(
                                Product("Product 1", 1.0),
                                Product("Product 2", 2.0),
                            ),
                            isDelivered = true,
                        ),
                        Order(
                            products = listOf(
                                Product("Product 3", 3.0),
                                Product("Product 4", 4.0),
                            ),
                            isDelivered = false,
                        ),
                    ),
                ),
                Customer(
                    name = "Customer 2",
                    city = City("City 2"),
                    orders = listOf(
                        Order(
                            products = listOf(
                                Product("Product 5", 5.0),
                                Product("Product 6", 6.0),
                            ),
                            isDelivered = true,
                        ),
                        Order(
                            products = listOf(
                                Product("Product 7", 7.0),
                                Product("Product 8", 8.0),
                            ),
                            isDelivered = true,
                        ),
                    ),
                ),
            ),
        )

        // when
        val result = shop.getWaitingCustomers()

        // then
        val expected = listOf(
            Customer(
                name = "Customer 1",
                city = City("City 1"),
                orders = listOf(
                    Order(
                        products = listOf(
                            Product("Product 1", 1.0),
                            Product("Product 2", 2.0),
                        ),
                        isDelivered = true,
                    ),
                    Order(
                        products = listOf(
                            Product("Product 3", 3.0),
                            Product("Product 4", 4.0),
                        ),
                        isDelivered = false,
                    ),
                ),
            ),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `getWaitingCustomers should not return customers with no orders`() {
        // given
        val shop = Shop(
            name = "Test shop",
            customers = listOf(
                Customer(
                    name = "Customer 1",
                    city = City("City 1"),
                    orders = listOf(
                        Order(
                            products = listOf(
                                Product("Product 1", 1.0),
                                Product("Product 2", 2.0),
                            ),
                            isDelivered = true,
                        ),
                        Order(
                            products = listOf(
                                Product("Product 3", 3.0),
                                Product("Product 4", 4.0),
                            ),
                            isDelivered = false,
                        ),
                    ),
                ),
                Customer(
                    name = "Customer 2",
                    city = City("City 2"),
                    orders = listOf(),
                ),
            ),
        )

        // when
        val result = shop.getWaitingCustomers()

        // then
        val expected = listOf(
            Customer(
                name = "Customer 1",
                city = City("City 1"),
                orders = listOf(
                    Order(
                        products = listOf(
                            Product("Product 1", 1.0),
                            Product("Product 2", 2.0),
                        ),
                        isDelivered = true,
                    ),
                    Order(
                        products = listOf(
                            Product("Product 3", 3.0),
                            Product("Product 4", 4.0),
                        ),
                        isDelivered = false,
                    ),
                ),
            ),
        )
        assertEquals(expected, result)
    }


    @Test
    fun `getWaitingCustomers should not return customers all orders delivered`() {
        // given
        val shop = Shop(
            name = "Test shop",
            customers = listOf(
                Customer(
                    name = "Customer 1",
                    city = City("City 1"),
                    orders = listOf(
                        Order(
                            products = listOf(
                                Product("Product 1", 1.0),
                                Product("Product 2", 2.0),
                            ),
                            isDelivered = true,
                        ),
                        Order(
                            products = listOf(
                                Product("Product 3", 3.0),
                                Product("Product 4", 4.0),
                            ),
                            isDelivered = true,
                        ),
                    ),
                ),
                Customer(
                    name = "Customer 2",
                    city = City("City 2"),
                    orders = listOf(
                        Order(
                            products = listOf(
                                Product("Product 5", 5.0),
                                Product("Product 6", 6.0),
                            ),
                            isDelivered = true,
                        ),
                        Order(
                            products = listOf(
                                Product("Product 7", 7.0),
                                Product("Product 8", 8.0),
                            ),
                            isDelivered = true,
                        ),
                    ),
                ),
            ),
        )

        // when
        val result = shop.getWaitingCustomers()

        // then
        assertEquals(listOf(), result)
    }

    @Test
    fun `countProductSales should count repeating sales`() {
        // given
        val p1 = Product("Product 1", 1.0)
        val p2 = Product("Product 2", 2.0)
        val p3 = Product("Product 3", 3.0)
        val shop = Shop(
            name = "Test shop",
            customers = listOf(
                Customer(
                    name = "Customer 1",
                    city = City("City 1"),
                    orders = listOf(
                        Order(
                            products = listOf(p1, p2),
                            isDelivered = true,
                        ),
                        Order(
                            products = listOf(p1, p3),
                            isDelivered = false,
                        ),
                    ),
                ),
                Customer(
                    name = "Customer 2",
                    city = City("City 2"),
                    orders = listOf(
                        Order(
                            products = listOf(p1, p1),
                            isDelivered = true,
                        ),
                        Order(
                            products = listOf(p2, p3),
                            isDelivered = true,
                        ),
                    ),
                ),
            ),
        )

        // then
        assertEquals(4, shop.countProductSales(p1))
        assertEquals(2, shop.countProductSales(p2))
        assertEquals(2, shop.countProductSales(p3))
    }

    @Test
    fun `countProductSales should count sales with exactly the same product`() {
        // given
        val shop = Shop(
            name = "Test shop",
            customers = listOf(
                Customer(
                    name = "Customer 1",
                    city = City("City 1"),
                    orders = listOf(
                        Order(
                            products = listOf(Product("Product 2", 1.0), Product("Product 1", 2.0)),
                            isDelivered = true,
                        ),
                    ),
                ),
            ),
        )

        // then
        assertEquals(0, shop.countProductSales(Product("Product 1", 1.0)))
        assertEquals(1, shop.countProductSales(Product("Product 2", 1.0)))
        assertEquals(1, shop.countProductSales(Product("Product 1", 2.0)))
        assertEquals(0, shop.countProductSales(Product("Product 2", 2.0)))
    }

    @Test
    fun `getCustomers should get customers with orders with total price greater than minAmount`() {
        // given
        val p1 = Product("Product 1", 1.0)
        val p2 = Product("Product 2", 2.0)
        val p3 = Product("Product 3", 3.0)
        val p4 = Product("Product 4", 4.0)
        val p5 = Product("Product 5", 5.0)
        val p6 = Product("Product 6", 6.0)
        val p7 = Product("Product 7", 7.0)
        val p8 = Product("Product 8", 8.0)
        val c1 = Customer(
            name = "Customer 1",
            city = City("City 1"),
            orders = listOf(
                Order(
                    products = listOf(p1, p2),
                    isDelivered = true,
                ),
                Order(
                    products = listOf(p1, p3),
                    isDelivered = false,
                ),
            ),
        )
        val c2 = Customer(
            name = "Customer 2",
            city = City("City 2"),
            orders = listOf(
                Order(
                    products = listOf(p1, p1),
                    isDelivered = true,
                ),
                Order(
                    products = listOf(p2, p3),
                    isDelivered = true,
                ),
            ),
        )
        val c3 = Customer(
            name = "Customer 3",
            city = City("City 3"),
            orders = listOf(
                Order(
                    products = listOf(p4, p5),
                    isDelivered = true,
                ),
                Order(
                    products = listOf(p6, p7),
                    isDelivered = true,
                ),
            ),
        )
        val c4 = Customer(
            name = "Customer 4",
            city = City("City 4"),
            orders = listOf(
                Order(
                    products = listOf(p8, p8),
                    isDelivered = true,
                ),
            ),
        )
        val shop = Shop(
            name = "Test shop",
            customers = listOf(c1, c2, c3, c4),
        )

        // then
        assertEquals(listOf(c1, c2, c3, c4), shop.getCustomers(0.0))
        assertEquals(listOf(c3, c4), shop.getCustomers(10.0))
        assertEquals(listOf(c3), shop.getCustomers(20.0))
        assertEquals(listOf(), shop.getCustomers(30.0))
    }
}
