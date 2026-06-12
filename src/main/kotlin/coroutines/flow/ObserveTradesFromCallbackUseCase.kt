package coroutines.flow.observetradesfromcallbackusecase

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

class ObserveTradesFromCallbackUseCase(
    private val api: TradeFeedApi,
) {
    fun observeTrades(): Flow<MarketEvent> = TODO()
}

data class Bid(
    val symbol: String,
    val price: Double,
    val amount: Double,
)

data class Ask(
    val symbol: String,
    val price: Double,
    val amount: Double,
)

data class Trade(
    val symbol: String,
    val price: Double,
    val amount: Double,
)

sealed class MarketEvent {
    data class BidEvent(val bid: Bid) : MarketEvent()
    data class AskEvent(val ask: Ask) : MarketEvent()
    data class TradeEvent(val trade: Trade) : MarketEvent()
}

interface TradeFeedCallback {
    fun onBid(bid: Bid)
    fun onAsk(ask: Ask)
    fun onTrade(trade: Trade)
    fun onCompleted()
    fun onApiError(cause: Throwable)
}

interface TradeFeedApi {
    fun register(callback: TradeFeedCallback)
    fun unregister(callback: TradeFeedCallback)
}

class FakeTradeFeedApi : TradeFeedApi {
    var registerCalls = 0
    var unregisterCalls = 0
    private var callback: TradeFeedCallback? = null

    override fun register(callback: TradeFeedCallback) {
        registerCalls++
        this.callback = callback
    }

    override fun unregister(callback: TradeFeedCallback) {
        unregisterCalls++
        if (this.callback === callback) {
            this.callback = null
        }
    }

    fun emitBid(bid: Bid) {
        callback?.onBid(bid)
    }

    fun emitAsk(ask: Ask) {
        callback?.onAsk(ask)
    }

    fun emitTrade(trade: Trade) {
        callback?.onTrade(trade)
    }

    fun complete() {
        callback?.onCompleted()
    }

    fun error(cause: Throwable) {
        callback?.onApiError(cause)
    }

    fun hasListener(): Boolean = callback != null
}

class ObserveTradesFromCallbackUseCaseTest {

    private suspend fun FakeTradeFeedApi.awaitListener() {
        withTimeout(1_000.milliseconds) {
            while (!hasListener()) {
                yield()
            }
        }
    }

    @Test
    fun `observeTrades registers listener on collection start`() = runTest {
        val api = FakeTradeFeedApi()
        val useCase = ObserveTradesFromCallbackUseCase(api)

        val job = launch {
            useCase.observeTrades().collect { }
        }

        api.awaitListener()

        assertEquals(1, api.registerCalls)
        assertTrue(api.hasListener())

        job.cancelAndJoin()
    }

    @Test
    fun `observeTrades forwards bid ask and trade as market events`() = runTest {
        val api = FakeTradeFeedApi()
        val useCase = ObserveTradesFromCallbackUseCase(api)

        val bid = Bid("BTCUSDT", 63000.0, 0.7)
        val ask = Ask("BTCUSDT", 63010.0, 0.5)
        val trade = Trade("BTCUSDT", 63005.0, 0.2)

        val expected = listOf(
            MarketEvent.BidEvent(bid),
            MarketEvent.AskEvent(ask),
            MarketEvent.TradeEvent(trade),
        )

        val collected = async { useCase.observeTrades().toList() }

        api.awaitListener()

        api.emitBid(bid)
        api.emitAsk(ask)
        api.emitTrade(trade)
        api.complete()

        assertContentEquals(expected, collected.await())
    }

    @Test
    fun `observeTrades closes flow on completed callback`() = runTest {
        val api = FakeTradeFeedApi()
        val useCase = ObserveTradesFromCallbackUseCase(api)

        val collected = async { useCase.observeTrades().toList() }

        api.awaitListener()

        api.complete()

        assertTrue(collected.await().isEmpty())
    }

    @Test
    fun `observeTrades propagates error as cancellation with original cause`() = runTest {
        val api = FakeTradeFeedApi()
        val useCase = ObserveTradesFromCallbackUseCase(api)
        val failure = IllegalStateException("socket disconnected")

        val collected = async {
            assertFailsWith<CancellationException> {
                useCase.observeTrades().toList()
            }
        }

        api.awaitListener()

        api.error(failure)
        val thrown = collected.await()

        val causes = generateSequence(thrown as Throwable) { it.cause }
        assertTrue(causes.any { it === failure })
    }

    @Test
    fun `observeTrades unregisters callback on cancellation`() = runTest {
        val api = FakeTradeFeedApi()
        val useCase = ObserveTradesFromCallbackUseCase(api)

        val job = launch {
            useCase.observeTrades().collect { awaitCancellation() }
        }

        api.awaitListener()

        assertTrue(api.hasListener())

        job.cancelAndJoin()

        assertEquals(1, api.unregisterCalls)
        assertFalse(api.hasListener())
    }

    @Test
    fun `observeTrades unregisters callback on completion and on error`() = runTest {
        val completionApi = FakeTradeFeedApi()
        val completionUseCase = ObserveTradesFromCallbackUseCase(completionApi)

        val completed = async { completionUseCase.observeTrades().toList() }

        completionApi.awaitListener()

        completionApi.complete()
        completed.await()

        assertEquals(1, completionApi.unregisterCalls)
        assertFalse(completionApi.hasListener())

        val errorApi = FakeTradeFeedApi()
        val errorUseCase = ObserveTradesFromCallbackUseCase(errorApi)
        val failed = async {
            assertFailsWith<CancellationException> {
                errorUseCase.observeTrades().toList()
            }
        }

        errorApi.awaitListener()

        errorApi.error(RuntimeException("boom"))
        failed.await()

        assertEquals(1, errorApi.unregisterCalls)
        assertFalse(errorApi.hasListener())
    }
}
