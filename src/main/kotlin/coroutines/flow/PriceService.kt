package coroutines.flow.priceservice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals

class PriceService(
    priceRepository: PriceRepository,
    backgroundScope: CoroutineScope,
) {
    fun observePrices(): Flow<Map<ProductId, PriceConfig>> =TODO()

    fun currentPrices(): Map<ProductId, PriceConfig> = TODO()
}

interface PriceRepository {
    fun observeUpdates(): Flow<Map<ProductId, PriceConfig>>
}

data class ProductId(val value: String)

class PriceConfig(
    val prices: Map<String, Map<Currency, BigDecimal>>,
)

enum class Currency {
    USD, EUR, GBP
}

class PriceServiceTest {
    val product1 = ProductId("1")
    val product2 = ProductId("2")
    val product3 = ProductId("3")
    val config1 = PriceConfig(mapOf("1" to mapOf(Currency.USD to BigDecimal("1.0"))))
    val config2 = PriceConfig(
        mapOf(
            "1" to mapOf(Currency.USD to BigDecimal("1.0")),
            "2" to mapOf(Currency.USD to BigDecimal("2.0"))
        )
    )
    val config3 = PriceConfig(mapOf("3" to mapOf(Currency.USD to BigDecimal("3.0"))))

    @Test
    fun `should send past prices`() = runTest {
        val priceRepository = FakePriceRepository()
        val priceService = PriceService(priceRepository, backgroundScope)
        delay(100)
        priceRepository.emitPrices(mapOf(product1 to config1))
        priceRepository.emitPrices(mapOf(product2 to config2))

        var observer1Updates = listOf<Pair<Long, Map<ProductId, PriceConfig>>>()
        priceService.observePrices()
            .onEach { observer1Updates = observer1Updates + (currentTime to it) }
            .launchIn(backgroundScope)

        runCurrent()
        assertEquals(
            listOf(100L to mapOf(product1 to config1, product2 to config2)),
            observer1Updates
        )
    }

    @Test
    fun `should send updates prices`() = runTest {
        val priceRepository = FakePriceRepository()
        val priceService = PriceService(priceRepository, backgroundScope)

        var observer1Updates = listOf<Pair<Long, Map<ProductId, PriceConfig>>>()
        priceService.observePrices()
            .onEach { observer1Updates = observer1Updates + (currentTime to it) }
            .launchIn(backgroundScope)

        delay(100)
        priceRepository.emitPrices(mapOf(product1 to config1))
        delay(100)
        priceRepository.emitPrices(mapOf(product2 to config2))

        runCurrent()
        assertEquals(
            listOf(
                100L to mapOf(product1 to config1),
                200L to mapOf(product2 to config2),
            ),
            observer1Updates
        )
    }

    @Test
    fun `should send past prices and then updates only`() = runTest {
        val priceRepository = FakePriceRepository()
        val priceService = PriceService(priceRepository, backgroundScope)
        delay(100)
        priceRepository.emitPrices(mapOf(product1 to config1))
        delay(100)
        priceRepository.emitPrices(mapOf(product2 to config2))

        var observer1Updates = listOf<Pair<Long, Map<ProductId, PriceConfig>>>()
        priceService.observePrices()
            .onEach { observer1Updates = observer1Updates + (currentTime to it) }
            .launchIn(backgroundScope)

        runCurrent()
        assertEquals(
            listOf(200L to mapOf(product1 to config1, product2 to config2)),
            observer1Updates
        )

        delay(100)
        priceRepository.emitPrices(mapOf(product3 to config3))

        var observer2Updates = listOf<Pair<Long, Map<ProductId, PriceConfig>>>()
        priceService.observePrices()
            .onEach { observer2Updates = observer2Updates + (currentTime to it) }
            .launchIn(backgroundScope)

        delay(100)
        priceRepository.emitPrices(mapOf(product1 to config2))

        runCurrent()
        assertEquals(
            listOf(
                200L to mapOf(product1 to config1, product2 to config2),
                300L to mapOf(product3 to config3),
                400L to mapOf(product1 to config2),
            ),
            observer1Updates
        )
        assertEquals(
            listOf(
                300L to mapOf(product1 to config1, product2 to config2, product3 to config3),
                400L to mapOf(product1 to config2),
            ),
            observer2Updates
        )
    }

    @Test
    fun `should reuse the same connection`() = runTest {
        val priceRepository = FakePriceRepository()
        val priceService = PriceService(priceRepository, backgroundScope)
        priceService.observePrices().launchIn(backgroundScope)
        priceService.observePrices().launchIn(backgroundScope)
        priceService.observePrices().launchIn(backgroundScope)
        runCurrent()
        assertEquals(1, priceRepository.observersCount())
    }
}

class FakePriceRepository : PriceRepository {
    private val observer = MutableSharedFlow<Map<ProductId, PriceConfig>>()
    private var observersCount = 0

    override fun observeUpdates(): Flow<Map<ProductId, PriceConfig>> = observer
        .onSubscription { observersCount++ }

    suspend fun emitPrices(prices: Map<ProductId, PriceConfig>) {
        observer.emit(prices)
    }

    fun observersCount() = observersCount
}
