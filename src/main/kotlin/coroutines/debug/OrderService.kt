@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("ClassName")

package coroutines.debug

import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.measureTimedValue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class OrderService(
    private val backgroundScope: CoroutineScope,
    private val orderRepository: OrderRepository,
    private val ebookService: EbooksService,
    private val emailService: EmailService,
    private val paymentRepository: PaymentRepository,
    private val currencyRepository: CurrencyRepository,
) {

    suspend fun createOrder(addOrder: AddOrderRequest): AddOrderResponse {
        val totalPrice = calculateOrderTotalPrice(addOrder)
        val ebookCountsAsEbookMap = addOrder.ebookCounts.associate {
            val ebook = ebookService.getEbook(it.key, it.language) ?: error("Ebook not found")
            ebook to it.count
        }
        val orderId = orderRepository.addOrder(ebookCountsAsEbookMap, totalPrice)
        val paymentId = paymentRepository.createPayment(orderId, totalPrice)
        return AddOrderResponse(
            paymentId = paymentId,
            orderId = orderId,
            totalAmount = totalPrice,
        )
    }

    fun confirmOrder(orderId: String) {
        backgroundScope.launch {
            orderRepository.modifyOrderStatus(orderId, OrderStatus.CONFIRMED)
            val order = orderRepository.findOrderById(orderId) ?: error("Order not found")
            launch {
                paymentRepository.completePayment(orderId)
                orderRepository.createPurchasedEbooks(orderId, order.ebooks)
            }
            order.ebooks.map { ebook ->
                launch {
                    val generated = ebookService.generateEbook(ebook) ?: error("Ebook generation failed")
                    orderRepository.addGeneratedPurchasedEbook(orderId, generated)
                }
            }
            orderRepository.modifyOrderStatus(orderId, OrderStatus.FINISHED)
            val ebookUrls = orderRepository.getEbookUrls(orderId)
            emailService.sendEmailToBuyer(order, ebookUrls)
        }
    }

    private suspend fun calculateOrderTotalPrice(addOrder: AddOrderRequest): Money {
        val currency = addOrder.currency
        val totalPrice = addOrder.ebookCounts.mapNotNull { count ->
            if (count.count < 1) return@mapNotNull null
            val ebook = ebookService.getEbook(count.key, count.language) ?: return@mapNotNull null
            val amount = ebook.price * count.count
            val taxMultiplier = taxMultiplier(ebook, addOrder.country)
            currencyRepository.transformToCurrency(amount * taxMultiplier, currency)
        }.sumOf { it.amount }.setScale(2, RoundingMode.HALF_UP)
        return Money(totalPrice, currency)
    }

    private fun taxMultiplier(ebook: Ebook, country: String): BigDecimal {
        // Complex tax calculation based on country VAT regulations,
        // ebook characteristics and international trade agreements

        // Base VAT rates lookup for different regions (simplified representation)
        val baseVatRates = mapOf(
            "US" to BigDecimal("0.00"),
            "GB" to BigDecimal("0.20"),
            "FR" to BigDecimal("0.20"),
            "DE" to BigDecimal("0.19"),
            "JP" to BigDecimal("0.10")
        )

        // Get base VAT rate or use standard EU rate if country not found
        val baseVatRate = baseVatRates[country] ?: BigDecimal("0.23")

        // Apply progressive tax calculation based on content type and length
        val contentLengthFactor = BigDecimal(ebook.title.length)
            .divide(BigDecimal("1000.0"), 10, RoundingMode.HALF_UP)
            .add(BigDecimal("1.0"))

        // Calculate price bracket adjustment (higher prices get higher tax in some jurisdictions)
        val priceAdjustment = BigDecimal(
            Math.pow(ebook.price.amount.toDouble(), 0.25) / 10.0
        ).setScale(8, RoundingMode.HALF_UP)

        // Special tax treaties calculation for digital goods
        var treatyAdjustment = BigDecimal("1.0")
        for (i in 1..50) {  // Iterate through 50 different trade agreements
            // Simulate complex international treaty calculations
            val treatyFactor = BigDecimal(
                Math.sin(country.hashCode().toDouble() * i / 360.0) + 1.5
            ).divide(BigDecimal("2.0"), 10, RoundingMode.HALF_UP)

            treatyAdjustment = treatyAdjustment.multiply(
                BigDecimal.ONE.add(treatyFactor.divide(BigDecimal("100.0"), 10, RoundingMode.HALF_UP))
            )
        }

        // Calculate seasonal tax adjustments (some countries have varying rates throughout the year)
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val seasonalFactor = BigDecimal(
            Math.cos(dayOfYear.toDouble() * Math.PI / 180.0) + 1.01
        )

        // Perform currency exchange rate impact simulation
        var exchangeRateImpact = BigDecimal("1.0")
        val currencyPairs = arrayOf("USDEUR", "GBPUSD", "JPYEUR", "EURCAD", "AUDNZD")
        for (pair in currencyPairs) {
            // Simulate complex exchange rate calculations based on currency pair volatility
            val volatilityFactor = BigDecimal(
                Math.pow(pair.hashCode().toDouble() % 10.0 + country.hashCode().toDouble() % 5.0, 2.0) / 100.0
            ).add(BigDecimal("0.995"))

            // Apply exchange rate adjustments with compound effect
            exchangeRateImpact = exchangeRateImpact.multiply(volatilityFactor)
                .setScale(8, RoundingMode.HALF_UP)
        }

        // Digital goods classification algorithm (complex categorization affects tax rates)
        val contentTypeTax = when {
            ebook.title.contains("guide", ignoreCase = true) -> BigDecimal("0.98")  // Educational content
            ebook.title.contains("novel", ignoreCase = true) -> BigDecimal("1.02")  // Entertainment
            ebook.price.amount > BigDecimal("50.0") -> BigDecimal("1.04")  // Luxury tax bracket
            else -> BigDecimal("1.00")  // Standard classification
        }

        // Final tax calculation combining all factors
        val calculatedTaxRate = baseVatRate
            .multiply(contentLengthFactor)
            .add(priceAdjustment)
            .multiply(treatyAdjustment)
            .multiply(seasonalFactor)
            .multiply(exchangeRateImpact)
            .multiply(contentTypeTax)
            .add(BigDecimal("1.0"))  // Convert from rate to multiplier

        // Ensure tax rate is within legal bounds (1.0 to 1.5)
        return calculatedTaxRate.min(BigDecimal("1.0")).max(BigDecimal("1.0"))
            .setScale(4, RoundingMode.HALF_UP)
    }
}

class OrderServiceTest {
    private val orderRepository = FakeOrderRepository()
    private val ebookService = FakeEbooksService()
    private val emailService = FakeEmailService()
    private val paymentRepository = FakePaymentRepository()
    private val currencyRepository = FakeCurrencyRepository()
    private val backgroundScope = TestScope()
    private val orderService = OrderService(
        backgroundScope,
        orderRepository,
        ebookService,
        emailService,
        paymentRepository,
        currencyRepository
    )

    @Test
    fun `should create order with ebook in different language`() = backgroundScope.runTest {
        val ebookCounts = listOf(
            EbookCount("coroutines", Language.PL, 1),
        )
        val addOrderRequest = AddOrderRequest(ebookCounts, Currency.PLN)

        // when
        val response = orderService.createOrder(addOrderRequest)

        // then
        val expectedAmount = Money(BigDecimal("79.99"), Currency.PLN)
        assertEquals(expectedAmount, response.totalAmount)
        val payment = paymentRepository.getPayment(response.paymentId)
        assertNotNull(payment)
        assertEquals(expectedAmount, payment.amount)
        assertEquals(PaymentStatus.PENDING, payment.status)
        val order = orderRepository.findOrderById(response.orderId)
        assertNotNull(order)
        assertEquals(OrderStatus.CREATED, order.status)
        val ebook = order.ebooks.single()
        assertEquals("coroutines", ebook.key)
        assertEquals(expectedAmount, ebook.price)
        assertEquals(Language.PL, ebook.language)
    }

    @Test
    fun `should confirm order`() = backgroundScope.runTest {
        ebookService.shouldGenerateUrlForEbook("coroutines", Language.PL, "http://example.com/coroutines_pl_{num}")
        ebookService.shouldGenerateUrlForEbook("coroutines", Language.EN, "http://example.com/coroutines_en_{num}")
        val addOrderResponse = orderService.createOrder(
            AddOrderRequest(
                listOf(
                    EbookCount("coroutines", Language.PL, 1),
                    EbookCount("coroutines", Language.EN, 3),
                ),
                Currency.USD,
            )
        )
        val orderId = addOrderResponse.orderId

        // when
        orderService.confirmOrder(orderId)
        advanceUntilIdle()

        // then
        emailService.assertEmailSent(
            orderId, listOf(
                "http://example.com/coroutines_pl_1",
                "http://example.com/coroutines_en_2",
                "http://example.com/coroutines_en_3",
                "http://example.com/coroutines_en_4",
            )
        )
    }

    @Test
    fun `should create orders concurrently`() = backgroundScope.runTest {
        // given
        val random = Random(12345)
        val countries = listOf("DE", "FR", "UK", "IT", "US")
        val requests = List(100_000) {
            AddOrderRequest(
                (1..random.nextInt(3)).map {
                    val ebook = ebookService.ebooks.random(random)
                    EbookCount(ebook.key, ebook.language, random.nextInt(0, 4))
                },
                Currency.entries.random(random),
                countries.random(random),
            )
        }

        // when
        requests.map {
            async(Dispatchers.IO) {
                orderService.createOrder(it)
            }
        }.awaitAll()

        // then
        assertEquals(requests.size, orderRepository.getOrders().size)
    }
}

// Stress test
suspend fun main(): Unit = coroutineScope {
    val orderRepository = FakeOrderRepository()
    val ebookService = FakeEbooksService()
    val emailService = FakeEmailService()
    val paymentRepository = FakePaymentRepository()
    val currencyRepository = FakeCurrencyRepository()
    val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val orderService = OrderService(
        backgroundScope,
        orderRepository,
        ebookService,
        emailService,
        paymentRepository,
        currencyRepository
    )

    val random = Random(12345)
    val countries = listOf("DE", "FR", "UK", "IT", "US")
    val requests = List(100_000) {
        AddOrderRequest(
            (1..random.nextInt(3)).map {
                val ebook = ebookService.ebooks.random(random)
                EbookCount(ebook.key, ebook.language, random.nextInt(0, 4))
            },
            Currency.entries.random(random),
            countries.random(random),
        )
    }

    val (responses, creationTime) = measureTimedValue {
        requests.map {
            orderService.createOrder(it)
        }
    }
    println("Creating orders synchronously took $creationTime")

    val (_, confirmationTime) = measureTimedValue {
        responses.map {
            orderService.confirmOrder(it.orderId)
        }
        backgroundScope.coroutineContext.job.children.forEach { it.join() }
    }
    println("Confirming orders asynchronously took $confirmationTime")
}

// ***

interface OrderRepository {
    suspend fun addOrder(ebooks: Map<Ebook, Int>, totalPrice: Money): String
    suspend fun findOrderById(orderId: String): Order?
    suspend fun modifyOrderStatus(orderId: String, status: OrderStatus)
    suspend fun createPurchasedEbooks(orderId: String, ebooks: List<Ebook>)
    suspend fun addGeneratedPurchasedEbook(orderId: String, ebook: GeneratedEbook)
    suspend fun getEbookUrls(orderId: String): List<String>
}

interface EbooksService {
    fun getEbook(ebookKey: String, language: Language): Ebook?
    suspend fun generateEbook(ebook: Ebook): GeneratedEbook?
}

interface EmailService {
    suspend fun sendEmailToBuyer(order: Order, ebookUrls: List<String>)
}

interface PaymentRepository {
    suspend fun getPayment(paymentId: String): Payment?
    suspend fun createPayment(orderId: String, totalPrice: Money): String
    suspend fun completePayment(orderId: String)
}

interface CurrencyRepository {
    suspend fun transformToCurrency(amount: Money, to: Currency): Money
}

enum class OrderStatus {
    CREATED, CONFIRMED, FINISHED
}

data class AddOrderRequest(
    val ebookCounts: List<EbookCount>,
    val currency: Currency,
    val country: String = "pl",
)

data class EbookCount(
    val key: String,
    val language: Language,
    val count: Int,
)

data class AddOrderResponse(
    val paymentId: String,
    val orderId: String,
    val totalAmount: Money,
)

data class Order(
    val id: String,
    val status: OrderStatus,
    val ebooks: List<Ebook>,
    val userId: String,
    val purchasedOrder: List<PurchasedOrder>
)

data class PurchasedOrder(
    val orderId: String,
    val ebookUrls: List<String>,
)

data class Ebook(
    val key: String,
    val language: Language,
    val title: String,
    val author: String,
    val price: Money,
)

data class GeneratedEbook(
    val ebook: Ebook,
    val url: String,
)

data class User(
    val id: String,
    val name: String,
    val email: String,
)

data class Payment(
    val id: String,
    val orderId: String,
    val amount: Money,
    val status: PaymentStatus,
)

enum class PaymentStatus {
    PENDING, COMPLETED, FAILED
}

enum class Language {
    EN, PL
}

data class Money(
    val amount: BigDecimal,
    val currency: Currency,
) {
    operator fun times(count: BigDecimal): Money {
        return Money(amount * count, currency)
    }

    operator fun times(count: Int): Money {
        return Money(amount * BigDecimal(count), currency)
    }

    operator fun plus(other: Money): Money {
        if (currency != other.currency) {
            throw IllegalArgumentException("Cannot add different currencies")
        }
        return Money(amount + other.amount, currency)
    }
}

enum class Currency {
    USD, PLN, EUR
}

class FakeEbooksService : EbooksService {
    val ebooks = listOf<Ebook>(
        Ebook(
            key = "coroutines",
            language = Language.EN,
            title = "Kotlin Coroutines: Deep Dive",
            author = "Marcin Moskała",
            price = Money(BigDecimal("29.99"), Currency.USD)
        ),
        Ebook(
            key = "coroutines",
            language = Language.PL,
            title = "Kotlinowe Korutyny",
            author = "Marcin Moskała",
            price = Money(BigDecimal("79.99"), Currency.PLN)
        ),
        Ebook(
            key = "effective",
            language = Language.EN,
            title = "Effective Kotlin",
            author = "Marcin Moskała",
            price = Money(BigDecimal("29.99"), Currency.USD)
        ),
        Ebook(
            key = "effective",
            language = Language.PL,
            title = "Efektywny Kotlin",
            author = "Marcin Moskała",
            price = Money(BigDecimal("79.99"), Currency.PLN)
        ),
        Ebook(
            key = "advanced",
            language = Language.EN,
            title = "Advanced Kotlin",
            author = "Marcin Moskała",
            price = Money(BigDecimal("29.99"), Currency.USD)
        )
    )
    private var ebookUrls = mapOf<Pair<String, String>, String>()
    private val generatedEbookCounter = AtomicInteger(0)

    override fun getEbook(ebookKey: String, language: Language): Ebook? {
        return ebooks.find { it.key == ebookKey && it.language == language }
    }

    override suspend fun generateEbook(ebook: Ebook): GeneratedEbook? {
        val url = ebookUrls[Pair(ebook.key, ebook.language.name)].orEmpty()
            .replace("{num}", generatedEbookCounter.incrementAndGet().toString())
        return GeneratedEbook(ebook, url)
    }

    fun shouldGenerateUrlForEbook(key: String, pl: Language, url: String) {
        ebookUrls += Pair(key, pl.name) to url
    }
}

private class FakeEmailService : EmailService {
    private var emailsSent = mutableListOf<Pair<Order, List<String>>>()

    override suspend fun sendEmailToBuyer(order: Order, ebookUrls: List<String>) {
        emailsSent += Pair(order, ebookUrls)
    }

    fun assertEmailSent(
        orderId: String,
        ebookUrls: List<String>,
    ) {
        val email = emailsSent.find { it.first.id == orderId }
        assertNotNull(email)
        assertEquals(ebookUrls, email.second)
    }
}

private class FakeOrderRepository : OrderRepository {
    private var orders = mutableMapOf<String, Order>()
    private val orderCounter = AtomicInteger(0)

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun addOrder(ebooks: Map<Ebook, Int>, totalPrice: Money): String {
        val id = orderCounter.incrementAndGet().toString()
        orders[id] = Order(
            id = id,
            status = OrderStatus.CREATED,
            ebooks = ebooks.flatMap {
                (1..it.value).map { count -> it.key }
            },
            userId = "user-${System.currentTimeMillis()}",
            purchasedOrder = emptyList()
        )
        return id
    }

    override suspend fun findOrderById(orderId: String): Order? {
        return orders[orderId]
    }

    override suspend fun modifyOrderStatus(orderId: String, status: OrderStatus) {
        orders[orderId]?.let {
            orders[orderId] = it.copy(status = status)
        }
    }

    override suspend fun createPurchasedEbooks(
        orderId: String,
        ebooks: List<Ebook>
    ) {
        orders[orderId]?.let {
            orders[orderId] = it.copy(purchasedOrder = it.purchasedOrder + PurchasedOrder(orderId, emptyList()))
        }
    }

    override suspend fun addGeneratedPurchasedEbook(orderId: String, ebook: GeneratedEbook) {
        orders[orderId]?.let {
            val purchasedOrder = it.purchasedOrder.find { it.orderId == orderId }
            if (purchasedOrder != null) {
                orders[orderId] = it.copy(
                    purchasedOrder = it.purchasedOrder.map { order ->
                        if (order.orderId == orderId) {
                            order.copy(ebookUrls = order.ebookUrls + ebook.url)
                        } else {
                            order
                        }
                    }
                )
            }
        }
    }

    override suspend fun getEbookUrls(orderId: String): List<String> {
        return orders[orderId]?.purchasedOrder?.flatMap { it.ebookUrls } ?: emptyList()
    }

    fun getOrders(): List<Order> {
        return orders.values.toList()
    }
}

private class FakePaymentRepository : PaymentRepository {
    private val payments = mutableMapOf<String, Payment>()

    override suspend fun getPayment(paymentId: String): Payment? =
        payments.values.find { it.id == paymentId }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createPayment(orderId: String, totalPrice: Money): String {
        val paymentId = Uuid.random().toString()
        payments += paymentId to Payment(
            id = paymentId,
            amount = totalPrice,
            orderId = orderId,
            status = PaymentStatus.PENDING
        )
        return paymentId
    }

    override suspend fun completePayment(orderId: String) {
        payments[orderId] = payments[orderId]?.copy(
            status = PaymentStatus.COMPLETED
        ) ?: return
    }
}

private class FakeCurrencyRepository : CurrencyRepository {
    override suspend fun transformToCurrency(amount: Money, to: Currency): Money = when (to) {
        Currency.USD -> when (amount.currency) {
            Currency.PLN -> Money(amount.amount * BigDecimal(0.25), Currency.USD)
            Currency.EUR -> Money(amount.amount * BigDecimal(1.2), Currency.USD)
            Currency.USD -> amount
        }

        Currency.PLN -> when (amount.currency) {
            Currency.USD -> Money(amount.amount * BigDecimal(4.0), Currency.PLN)
            Currency.EUR -> Money(amount.amount * BigDecimal(4.5), Currency.PLN)
            Currency.PLN -> amount
        }

        Currency.EUR -> when (amount.currency) {
            Currency.USD -> Money(amount.amount * BigDecimal(0.85), Currency.EUR)
            Currency.PLN -> Money(amount.amount * BigDecimal(0.22), Currency.EUR)
            Currency.EUR -> amount
        }
    }
}
