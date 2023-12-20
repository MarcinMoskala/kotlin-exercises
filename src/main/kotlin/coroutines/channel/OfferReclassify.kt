//package channel
//
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.channels.Channel
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.supervisorScope
//import kotlinx.coroutines.sync.Mutex
//import kotlinx.coroutines.sync.withLock
//import kotlinx.coroutines.test.currentTime
//import kotlinx.coroutines.test.runTest
//import javax.naming.ServiceUnavailableException
//import kotlin.random.Random
//import kotlin.test.Test
//import kotlin.test.assertEquals
//
//class OfferReclassify(
//    private val offerStoreApi: OfferStoreApi,
//    private val sendEventApi: SendEventApi,
//    private val reclassifyConcurrency: Int,
//    private val scope: CoroutineScope
//) {
//
//    suspend fun reclassifyAllOffers() = supervisorScope {
//        val offers = Channel<OfferId>(capacity = Channel.UNLIMITED)
//        launch {
//            var last: OfferId? = null
//            while (true) {
//                val next = offerStoreApi.getNextOfferIds(last)
//                if (next.isEmpty()) break
//                next.forEach { offers.send(it) }
//                last = next.last()
//            }
//        }
//
//        repeat(reclassifyConcurrency) {
//            launch {
//                for (offer in offers) {
//                    try {
//                        sendEventApi.reclassifyOffer(offer)
//                    } catch (e: ServiceUnavailableException) {
//                        offers.send(offer)
//                    }
//                }
//            }
//        }
//    }
//}
//
//interface OfferStoreApi {
//    suspend fun getNextOfferIds(last: OfferId?): List<OfferId>
//}
//
//interface SendEventApi {
//    @Throws(ServiceUnavailableException::class)
//    suspend fun reclassifyOffer(id: OfferId)
//}
//
//data class OfferId(val raw: String)
//
//class TestOfferStoreApi(
//    private val callDelay: Long = 0,
//    private val offersNumber: Int = 1000,
//) : OfferStoreApi {
//    val allIds = List(offersNumber) { OfferId("Offer$it") }
//    private val mutex = Mutex()
//
//    override suspend fun getNextOfferIds(last: OfferId?): List<OfferId> = mutex.withLock {
//        delay(callDelay)
//        val remaining = if (last == null) allIds else allIds.dropWhile { it != last }.drop(1)
//        return remaining.take(10)
//    }
//}
//
//class TestSendEventApi(
//    private val sometimesFailing: Boolean = false,
//    private val callDelay: Long = 0,
//) : SendEventApi {
//    var reclassified = listOf<OfferId>()
//    private val mutex = Mutex()
//
//    override suspend fun reclassifyOffer(id: OfferId) = mutex.withLock {
//        delay(callDelay)
//        if (sometimesFailing && Random.nextBoolean()) {
//            throw ServiceUnavailableException()
//        }
//        reclassified = reclassified + id
//    }
//}
//
//class OfferReclassifyTest {
//
//    @Test
//    fun `Should reclassify all elements`() = runTest {
//        val offerStoreApi = TestOfferStoreApi()
//        val sendEventApi = TestSendEventApi()
//        val useCase = OfferReclassify(offerStoreApi, sendEventApi, reclassifyConcurrency = 10)
//        useCase.reclassifyAllOffers()
//        assertEquals(offerStoreApi.allIds.sortedBy { it.raw }, sendEventApi.reclassified.sortedBy { it.raw })
//    }
//
//    @Test
//    fun `Should get and reclassify asynchroniously`() = runTest {
//        val callDelay = 10L
//        val offerStoreApi = TestOfferStoreApi(callDelay = callDelay)
//        val sendEventApi = TestSendEventApi(callDelay = callDelay)
//        val useCase = OfferReclassify(offerStoreApi, sendEventApi, reclassifyConcurrency = 10)
//        useCase.reclassifyAllOffers()
//        assertEquals(1001 * callDelay, currentTime)
//    }
//
//    @Test
//    fun `should try to reclassify failed offers again`() = runTest {
//        val offerStoreApi = TestOfferStoreApi()
//        val sendEventApi = TestSendEventApi(sometimesFailing = true)
//        val useCase = OfferReclassify(offerStoreApi, sendEventApi, reclassifyConcurrency = 10)
//        useCase.reclassifyAllOffers()
//        assertEquals(offerStoreApi.allIds.size, sendEventApi.reclassified.size)
//    }
//
//    @Test
//    fun `should limit reclassify concurrency`() = runTest {
//        val callDelay = 1000L
//        val concurrency = 100
//        val offersNumber = 1000
//        val offerStoreApi = TestOfferStoreApi(
//            offersNumber = offersNumber,
//        )
//        val sendEventApi = TestSendEventApi(
//            callDelay = callDelay,
//        )
//        val useCase = OfferReclassify(offerStoreApi, sendEventApi, reclassifyConcurrency = concurrency)
//        useCase.reclassifyAllOffers()
//        assertEquals(offersNumber * callDelay / concurrency, currentTime)
//    }
//}
