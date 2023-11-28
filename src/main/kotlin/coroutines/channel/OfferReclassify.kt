package channel

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import javax.naming.ServiceUnavailableException
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

suspend fun reclassifyAllOffers(offerStoreApi: OfferStoreApi, sendEventApi: SendEventApi) = coroutineScope<Unit> {
    TODO()
}

interface OfferStoreApi {
    suspend fun getNextOfferIds(last: OfferId?): List<OfferId>
}

interface SendEventApi {
    @Throws(ServiceUnavailableException::class)
    suspend fun reclassifyOffer(id: OfferId)
}

data class OfferId(val raw: String)

class TestOfferStoreApi(private val callDelay: Long = 0) : OfferStoreApi {
    val allIds = List(1000) { OfferId("Offer$it") }
    private val mutex = Mutex()

    override suspend fun getNextOfferIds(last: OfferId?): List<OfferId> = mutex.withLock {
        delay(callDelay)
        val remaining = if (last == null) allIds else allIds.dropWhile { it != last }.drop(1)
        return remaining.take(10)
    }
}

class TestSendEventApi(sometimesFailing: Boolean = false, private val callDelay: Long = 0) : SendEventApi {
    val allIds = List(1000) { OfferId("Offer$it") }
    var failingIds = if (sometimesFailing) allIds.shuffled().take(10) else emptyList()
    var reclassified = listOf<OfferId>()
    private val mutex = Mutex()

    override suspend fun reclassifyOffer(id: OfferId) = mutex.withLock {
        delay(callDelay)
        if (id in failingIds) {
            if (Random.nextBoolean()) failingIds = failingIds - id
            throw ServiceUnavailableException()
        }
        reclassified = reclassified + id
    }
}

class OfferReclassifyTest {

    @Test
    fun `Should reclassify all elements`() = runTest {
        val offerStoreApi = TestOfferStoreApi()
        val sendEventApi = TestSendEventApi()
        reclassifyAllOffers(offerStoreApi, sendEventApi)
        assertEquals(sendEventApi.allIds.sortedBy { it.raw }, sendEventApi.reclassified.sortedBy { it.raw })
    }

    @Test
    fun `Should get and reclassify asynchroniously`() = runTest {
        val callDelay = 10L
        val offerStoreApi = TestOfferStoreApi(callDelay = callDelay)
        val sendEventApi = TestSendEventApi(callDelay = callDelay)
        val before = currentTime
        reclassifyAllOffers(offerStoreApi, sendEventApi)
        val after = currentTime
        assertEquals(1001 * callDelay, after - before)
    }

    // Extra
//    @Test
//    fun `Should try to classify again if failed`() = runTest {
//        val offerStoreApi = TestOfferStoreApi()
//        val sendEventApi = TestSendEventApi(sometimesFailing = true)
//        reclassifyAllOffers(offerStoreApi, sendEventApi)
//        assertEquals(sendEventApi.allIds.sortedBy { it.raw }, sendEventApi.reclassified.sortedBy { it.raw })
//    }
}