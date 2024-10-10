package coroutines.flow.productservice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    fun observeProductUpdates(): Flow<String>
    suspend fun fetchProduct(id: String): Product
}

data class Product(
    val id: String,
    val category: String,
    val name: String,
    val price: Double,
)

@ExperimentalCoroutinesApi
class ProductServiceTest {

    @Test
    fun `should emit distinct products`() = runTest {
        val fakeProductRepository = FakeProductRepository()
        val productService = ProductService(fakeProductRepository, backgroundScope)
        val categories = setOf("electronics")
        val observedProducts = mutableListOf<Product>()
        productService.observeProducts(categories)
            .onEach { observedProducts.add(it) }
            .launchIn(backgroundScope)

        runCurrent()
        fakeProductRepository.updatesHasOccurred("1", "1", "2", "3")
        runCurrent()

        assertEquals(1, observedProducts.size)
        assertEquals("1", observedProducts[0].id)
    }

    @Test
    fun `should filter products by category`() = runTest {
        val fakeProductRepository = FakeProductRepository()
        val productService = ProductService(fakeProductRepository, backgroundScope)
        val categories = setOf("books")
        val observedProducts = mutableListOf<Product>()
        productService.observeProducts(categories)
            .onEach { observedProducts.add(it) }
            .launchIn(backgroundScope)

        runCurrent()
        fakeProductRepository.updatesHasOccurred("1", "1", "2", "3")
        runCurrent()

        assertEquals(1, observedProducts.size)
        assertEquals("2", observedProducts[0].id)
    }

    @Test
    fun `should map product IDs to products`() = runTest {
        val fakeProductRepository = FakeProductRepository()
        val productService = ProductService(fakeProductRepository, backgroundScope)
        val categories = setOf("electronics", "books")
        val observedProducts = mutableListOf<Product>()
        productService.observeProducts(categories)
            .onEach { observedProducts.add(it) }
            .launchIn(backgroundScope)

        runCurrent()
        fakeProductRepository.updatesHasOccurred("1", "1", "2", "3")
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

        val job1 = backgroundScope.launch { productService.observeProducts(setOf("electronics")).collect() }
        runCurrent()
        assertEquals(1, productService.activeObserversCount())
        val job2 = backgroundScope.launch { productService.observeProducts(setOf("electronics")).collect() }
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
        val categories = setOf("electronics")
        val observedProducts = mutableListOf<Product>()
        val job = backgroundScope.launch {
            productService.observeProducts(categories)
                .onEach { observedProducts.add(it) }
                .collect()
        }
        runCurrent()
        fakeProductRepository.updatesHasOccurred("1", "1", "2", "3")
        runCurrent()

        assertEquals(1, observedProducts.size)
        assertEquals("1", observedProducts[0].id)
        job.cancel()
        runCurrent()
        fakeProductRepository.updatesHasOccurred("1", "1", "2", "3")
        runCurrent()

        assertEquals(1, observedProducts.size)
    }

    @Test
    fun `should fetch products concurrently`() = runTest {
        val fakeProductRepository = FakeProductRepository(fetchProductsDelay = 1000)
        val productService = ProductService(fakeProductRepository, backgroundScope)
        val categories = setOf("electronics", "books")
        val observedProducts = mutableListOf<Product>()
        productService.observeProducts(categories)
            .onEach { observedProducts.add(it) }
            .launchIn(backgroundScope)

        runCurrent()
        fakeProductRepository.updatesHasOccurred("1", "1", "2", "3")
        advanceUntilIdle()
        do {
            delay(100)
            runCurrent()
        } while (observedProducts.size < 2)

        assertEquals(1000, currentTime)
    }
}

class FakeProductRepository(
    private val fetchProductsDelay: Long = 0,
) : ProductRepository {
    private val products = listOf(
        Product("1", "electronics", "Smartphone", 500.0),
        Product("2", "books", "Novel", 20.0),
        Product("3", "clothing", "T-Shirt", 15.0)
    )
    private val updates = MutableSharedFlow<String>()

    override fun observeProductUpdates(): Flow<String> = updates

    override suspend fun fetchProduct(id: String): Product {
        if (fetchProductsDelay > 0) {
            delay(fetchProductsDelay)
        }
        return products.first { it.id == id }
    }

    suspend fun updatesHasOccurred(vararg ids: String) {
        ids.forEach { updates.emit(it) }
    }
}
