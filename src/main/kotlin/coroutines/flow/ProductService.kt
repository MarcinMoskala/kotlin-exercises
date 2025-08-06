package coroutines.flow.productservice

import coroutines.flow.productservice.TestData.product1
import coroutines.flow.productservice.TestData.product2
import coroutines.flow.productservice.TestData.product3
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ProductService(
    private val productRepository: ProductRepository,
    backgroundScope: CoroutineScope,
) {
    private val activeObservers = AtomicInteger(0)

    fun observeProducts(categories: Set<String>): Flow<Product> = TODO()

    fun activeObserversCount(): Int = activeObservers.get()
}

interface ProductRepository {
    // Emits ids of the products that got updated
    fun observeProductUpdates(): Flow<String>
    suspend fun fetchProduct(id: String): Product
}

data class Product(
    val id: String,
    val category: String,
    val name: String,
    val price: Double,
)

      class ProductServiceTest {

    @Test
    fun `should emit distinct products`() = runTest {
        val fakeProductRepository = FakeProductRepository()
        val productService = ProductService(fakeProductRepository, backgroundScope)
        val categories = setOf(product1.category)
        val observedProducts = mutableListOf<Product>()
        productService.observeProducts(categories)
            .onEach { observedProducts.add(it) }
            .launchIn(backgroundScope)

        runCurrent()
        fakeProductRepository.updatesHasOccurred(product1.id, product1.id, product2.id, product3.id)
        runCurrent()

        assertEquals(1, observedProducts.size)
        assertEquals("1", observedProducts[0].id)
    }

    @Test
    fun `should filter products by category`() = runTest {
        val fakeProductRepository = FakeProductRepository()
        val productService = ProductService(fakeProductRepository, backgroundScope)
        val categories = setOf(product2.category)
        val observedProducts = mutableListOf<Product>()
        productService.observeProducts(categories)
            .onEach { observedProducts.add(it) }
            .launchIn(backgroundScope)

        runCurrent()
        fakeProductRepository.updatesHasOccurred(product1.id, product1.id, product2.id, product3.id)
        runCurrent()

        assertEquals(1, observedProducts.size)
        assertEquals("2", observedProducts[0].id)
    }

    @Test
    fun `should map product IDs to products`() = runTest {
        val fakeProductRepository = FakeProductRepository()
        val productService = ProductService(fakeProductRepository, backgroundScope)
        val categories = setOf(product1.category, product2.category)
        val observedProducts = mutableListOf<Product>()
        productService.observeProducts(categories)
            .onEach { observedProducts.add(it) }
            .launchIn(backgroundScope)

        runCurrent()
        fakeProductRepository.updatesHasOccurred(product1.id, product1.id, product2.id, product3.id)
        runCurrent()

        assertEquals(2, observedProducts.size)
        assertEquals("Smartphone", observedProducts[0].name)
        assertEquals("Novel", observedProducts[1].name)
    }

    @Test
    fun `should count active observers`() = runTest {
        val fakeProductRepository = FakeProductRepository()
        val productService = ProductService(fakeProductRepository, backgroundScope)
        assertEquals(0, productService.activeObserversCount())

        val job1 = backgroundScope.launch { productService.observeProducts(setOf(product1.category)).collect() }
        runCurrent()
        assertEquals(1, productService.activeObserversCount())
        val job2 = backgroundScope.launch { productService.observeProducts(setOf(product1.category)).collect() }
        runCurrent()
        assertEquals(2, productService.activeObserversCount())
        job2.cancel()
        runCurrent()
        assertEquals(1, productService.activeObserversCount())
        job1.cancel()
        runCurrent()
        assertEquals(0, productService.activeObserversCount())
    }

    @Test
    fun `should not complete when there are active observers`() = runTest {
        val fakeProductRepository = FakeProductRepository()
        val productService = ProductService(fakeProductRepository, backgroundScope)
        val categories = setOf(product1.category)
        val observedProducts = mutableListOf<Product>()
        val job = backgroundScope.launch {
            productService.observeProducts(categories)
                .onEach { observedProducts.add(it) }
                .collect()
        }
        runCurrent()
        fakeProductRepository.updatesHasOccurred(product1.id, product1.id, product2.id, product3.id)
        runCurrent()

        assertEquals(1, observedProducts.size)
        assertEquals("1", observedProducts[0].id)
        job.cancel()
        runCurrent()
        fakeProductRepository.updatesHasOccurred(product1.id, product1.id, product2.id, product3.id)
        runCurrent()

        assertEquals(1, observedProducts.size)
    }

    @Test
    fun `should fetch products concurrently`() = runTest {
        val fakeProductRepository = FakeProductRepository(fetchProductsDelay = 1000)
        val productService = ProductService(fakeProductRepository, backgroundScope)
        val categories = setOf(product1.category, product2.category)
        val observedProducts = Channel<Product>(Channel.UNLIMITED)
        productService.observeProducts(categories)
            .onEach { observedProducts.send(it) }
            .launchIn(backgroundScope)

        runCurrent()
        fakeProductRepository.updatesHasOccurred(product1.id, product1.id, product2.id, product3.id)

        // waiting for all products
        observedProducts.consumeAsFlow().take(2).collect()

        // should take as much as a single fetch
        assertEquals(1000, currentTime)
    }

    @Test
    fun `should not limit the number of fetched products`() = runTest {
        val products = List(1000) { // Big number of products to fetch
            Product(
                id = "id$it",
                category = "ALL",
                name = "name$it",
                price = it.toDouble(),
            )
        }
        val fakeProductRepository = FakeProductRepository(fetchProductsDelay = 1000, products = products)
        val productService = ProductService(fakeProductRepository, backgroundScope)
        val observedProducts = Channel<Product>(Channel.UNLIMITED)
        productService.observeProducts(setOf("ALL"))
            .onEach { observedProducts.send(it) }
            .launchIn(backgroundScope)

        runCurrent()
        fakeProductRepository.updatesHasOccurred(*products.map { it.id }.toTypedArray())
        advanceUntilIdle()

        // waiting for all products
        observedProducts.consumeAsFlow().take(products.size).collect()

        // should take as much as a single fetch
        assertEquals(1000, currentTime)
    }
}

object TestData {
    val product1 = Product("1", "electronics", "Smartphone", 500.0)
    val product2 = Product("2", "books", "Novel", 20.0)
    val product3 = Product("3", "clothing", "T-Shirt", 15.0)
}

class FakeProductRepository(
    private val fetchProductsDelay: Long = 0,
    private val products: List<Product> = listOf(
        product1,
        product2,
        product3
    )
) : ProductRepository {
    private val updates = MutableSharedFlow<String>()
    var observers = 0
        private set
    var productFetchCounter = 0
        private set

    override fun observeProductUpdates(): Flow<String> = updates
        .onStart { observers++ }
        .onCompletion { observers-- }

    override suspend fun fetchProduct(id: String): Product {
        productFetchCounter++
        if (fetchProductsDelay > 0) {
            delay(fetchProductsDelay)
        }
        return products.first { it.id == id }
    }

    suspend fun updatesHasOccurred(vararg ids: String) {
        ids.forEach { updates.emit(it) }
    }
}
