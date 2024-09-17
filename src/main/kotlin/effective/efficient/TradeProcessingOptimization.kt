package effective.efficient

import effective.efficient.Filter.*
import effective.efficient.Filter.Relation.*
import effective.efficient.Filter.SnapshotPart.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

data class TickerSnapshot(
    val ticker: Ticker,
    val snapshot: Snapshot,
)

data class Snapshot(
    val bid: PriceSizeTime?,
    val ask: PriceSizeTime?,
    val last: PriceSizeTime?,
)

data class PriceSizeTime(
    val price: Price,
    val size: Int? = null,
    val time: Long? = null,
)

data class Ticker(val value: String)
data class Price(val value: Float?)

sealed interface Event {
    val ticker: String
}

data class BidEvent(override val ticker: String, val price: Float?, val size: Int?, val time: Long?) : Event
data class AskEvent(override val ticker: String, val price: Float?, val size: Int?, val time: Long?) : Event
data class TradeEvent(override val ticker: String, val price: Float?, val size: Int?, val time: Long?) : Event

val tickers = List(1000) { "Ticker$it" }

// Do not touch this one
class MarketClient {
    fun observe(): Flow<Event> = flow {
        val random = Random(123456789)
        while (true) {
            val event = when ((0..2).random(random)) {
                0 -> BidEvent(
                    tickers.random(random),
                    if (random.nextInt(100) == 1) null else random.nextInt(100_000).toFloat(),
                    if (random.nextInt(100) == 1) null else random.nextInt(100_000),
                    if (random.nextInt(100) == 1) null else random.nextLong()
                )

                1 -> AskEvent(
                    tickers.random(random),
                    if (random.nextInt(100) == 1) null else random.nextInt(100_000).toFloat(),
                    if (random.nextInt(100) == 1) null else random.nextInt(100_000),
                    if (random.nextInt(100) == 1) null else random.nextLong()
                )

                else -> TradeEvent(
                    tickers.random(random),
                    if (random.nextInt(100) == 1) null else random.nextInt(100_000).toFloat(),
                    if (random.nextInt(100) == 1) null else random.nextInt(100_000),
                    if (random.nextInt(100) == 1) null else random.nextLong()
                )
            }
            emit(event)
        }
    }
}

class MarketRepository(
    private val client: MarketClient,
    backgroundScope: CoroutineScope,
) {
    private val snapshots = ConcurrentHashMap<Ticker, Snapshot>()
    private val updates = MutableSharedFlow<TickerSnapshot>()

    fun observeUpdates() = updates
        .onStart { snapshots.forEach { emit(TickerSnapshot(it.key, it.value)) } }

    init {
        backgroundScope.launch {
            client.observe().collect {
                when (it) {
                    is BidEvent -> {
                        val snapshot = snapshots.getOrPut(Ticker(it.ticker)) { Snapshot(null, null, null) }
                            .copy(bid = PriceSizeTime(Price(it.price), it.size, it.time))
                        snapshots[Ticker(it.ticker)] = snapshot
                        updates.emit(TickerSnapshot(Ticker(it.ticker), snapshot))
                    }

                    is AskEvent -> {
                        val snapshot = snapshots.getOrPut(Ticker(it.ticker)) { Snapshot(null, null, null) }
                            .copy(ask = PriceSizeTime(Price(it.price), it.size, it.time))
                        snapshots[Ticker(it.ticker)] = snapshot
                        updates.emit(TickerSnapshot(Ticker(it.ticker), snapshot))
                    }

                    is TradeEvent -> {
                        val snapshot = snapshots.getOrPut(Ticker(it.ticker)) { Snapshot(null, null, null) }
                            .copy(last = PriceSizeTime(Price(it.price), it.size, it.time))
                        snapshots[Ticker(it.ticker)] = snapshot
                        updates.emit(TickerSnapshot(Ticker(it.ticker), snapshot))
                    }
                }
            }
        }
    }
}

sealed class Filter {
    data object All : Filter()
    class Or(val filters: List<Filter>) : Filter()
    class And(val filters: List<Filter>) : Filter()
    class PrizeCondition(
        val snapshotPart: SnapshotPart,
        val relation: Relation,
        val value: Float,
    ) : Filter()

    class TickerIs(val tickers: List<Ticker>) : Filter()
    class Not(val filter: Filter) : Filter()

    enum class SnapshotPart {
        Ask, Bid, Last, Spread
    }

    enum class Relation {
        GreaterThan, LessThan, Equal
    }

    fun check(tickerSnapshot: TickerSnapshot): Boolean = when (this) {
        All -> true
        is Or -> filters.any { it.check(tickerSnapshot) }
        is And -> filters.all { it.check(tickerSnapshot) }
        is PrizeCondition -> run {
            val snapshotPrize = when (snapshotPart) {
                Ask -> tickerSnapshot.snapshot.ask?.price?.value ?: return@run false
                Bid -> tickerSnapshot.snapshot.bid?.price?.value ?: return@run false
                Last -> tickerSnapshot.snapshot.last?.price?.value ?: return@run false
                Spread -> {
                    val bid = tickerSnapshot.snapshot.bid?.price?.value ?: return@run false
                    val ask = tickerSnapshot.snapshot.ask?.price?.value ?: return@run false
                    ask - bid
                }
            }
            when (relation) {
                GreaterThan -> snapshotPrize > value
                LessThan -> snapshotPrize < value
                Equal -> snapshotPrize == value
            }
        }

        is TickerIs -> tickers.contains(tickerSnapshot.ticker)
        is Not -> !filter.check(tickerSnapshot)
    }
}

class TradeService(
    private val repository: MarketRepository,
) {
    fun observeUpdates(
        filter: Filter? = null,
        tickers: List<String>? = null,
    ) = repository.observeUpdates()
        .filter { tickers == null || it.ticker.value in tickers }
        .filter { filter == null || filter.check(it) }
}

suspend fun main() {
    val client = MarketClient()
    val repository = MarketRepository(client, backgroundScope = CoroutineScope(SupervisorJob()))
    val service = TradeService(repository)
    val filter = Or(
        listOf(
            And(listOf(TickerIs(tickers.take(1).map(::Ticker)), PrizeCondition(Ask, GreaterThan, 99f))),
            And(listOf(PrizeCondition(Spread, GreaterThan, 99f))),
        )
    )

    measureTimeMillis {
        service.observeUpdates(
            filter = filter,
            tickers = tickers.take(70)
        ).take(1_000)
            .collect { println(it) }
    }.let { println("Took $it") }
}

class TradeProcessingOptimizationConsistencyTest {
    @Test
    fun resultTest() = runTest {
        val client = MarketClient()
        val repository = MarketRepository(client, backgroundScope = CoroutineScope(SupervisorJob()))
        val service = TradeService(repository)
        val filter = Or(
            listOf(
                And(listOf(TickerIs(tickers.take(1).map(::Ticker)), PrizeCondition(Ask, GreaterThan, 99f))),
                And(listOf(PrizeCondition(Spread, GreaterThan, 99f))),
            )
        )

        val result: List<TickerSnapshot> = service.observeUpdates(
            filter = filter,
            tickers = tickers.take(70)
        ).take(3)
            .toList()

        val expected = listOf<TickerSnapshot>(
            TickerSnapshot(
                ticker = Ticker(value = "Ticker10"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(
                        price = Price(value = 0.0f),
                        size = 75,
                        time = 6841553342386962486
                    ),
                    ask = PriceSizeTime(price = Price(value = 100.0f), size = 9, time = -9084042298188709619),
                    last = PriceSizeTime(price = Price(value = 27.0f), size = 96, time = -373739100953929578)
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker0"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(
                        price = Price(value = 88.0f),
                        size = 49,
                        time = -3464155828371484883
                    ),
                    ask = PriceSizeTime(price = Price(value = 100.0f), size = 64, time = -1685381896913982804),
                    last = PriceSizeTime(price = Price(value = 67.0f), size = 75, time = -2705495291132989550)
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker0"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(
                        price = Price(value = 88.0f),
                        size = 49,
                        time = -3464155828371484883
                    ),
                    ask = PriceSizeTime(price = Price(value = 100.0f), size = 64, time = -1685381896913982804),
                    last = PriceSizeTime(price = Price(value = 57.0f), size = 44, time = 3271983615007230067)
                )
            )
        )
        assertEquals(expected, result)
    }

    @Test
    fun anyTest() = runTest {
        val client = MarketClient()
        val repository = MarketRepository(client, backgroundScope = CoroutineScope(SupervisorJob()))
        val service = TradeService(repository)

        val result: List<TickerSnapshot> = service.observeUpdates()
            .take(500)
            .drop(490)
            .toList()

        val expected = listOf<TickerSnapshot>(
            TickerSnapshot(
                ticker = Ticker(value = "Ticker599"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(
                        price = Price(value = 3.0f),
                        size = 32,
                        time = -7154366676655009910
                    ),
                    ask = null,
                    last = PriceSizeTime(price = Price(value = 32.0f), size = 35, time = 8119465514165684939)
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker792"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(
                        price = Price(value = 57.0f),
                        size = 51,
                        time = 9046446809172565568
                    ), ask = null, last = null
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker767"),
                snapshot = Snapshot(
                    bid = null,
                    ask = PriceSizeTime(price = Price(value = 90.0f), size = 50, time = -3981946300853867545),
                    last = null
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker44"),
                snapshot = Snapshot(
                    bid = null,
                    ask = null,
                    last = PriceSizeTime(price = Price(value = 2.0f), size = 59, time = 8928513245163532476)
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker439"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(
                        price = Price(value = 80.0f),
                        size = 15,
                        time = 5705359404818493164
                    ),
                    ask = PriceSizeTime(price = Price(value = 23.0f), size = 88, time = 2210899662547177636),
                    last = null
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker715"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(
                        price = Price(value = 91.0f),
                        size = 22,
                        time = 674890693049164191
                    ),
                    ask = PriceSizeTime(price = Price(value = 89.0f), size = 6, time = -4671874103540276010),
                    last = null
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker847"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(
                        price = Price(value = 6.0f),
                        size = 65,
                        time = -1342033315582348433
                    ),
                    ask = PriceSizeTime(price = Price(value = 90.0f), size = 2, time = -1458896883356733664),
                    last = null
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker530"),
                snapshot = Snapshot(
                    bid = null,
                    ask = null,
                    last = PriceSizeTime(price = Price(value = 18.0f), size = 88, time = 5655792291667859376)
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker832"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(
                        price = Price(value = 3.0f),
                        size = 87,
                        time = 5723479612060452388
                    ), ask = null, last = null
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker602"),
                snapshot = Snapshot(
                    bid = null,
                    ask = null,
                    last = PriceSizeTime(price = Price(value = 40.0f), size = 68, time = 4176048430149649671)
                )
            )
        )
        assertEquals(expected, result)
    }

    @Test
    fun consistencyMarketClient() = runTest {
        val client = MarketClient()
        val actual = client.observe().drop(1000).take(8).toList()

        val expected: List<Event> = listOf(
            BidEvent(ticker = "Ticker104", price = 84.0f, size = 62, time = -3918742120704651959),
            BidEvent(ticker = "Ticker217", price = 28.0f, size = 39, time = -8444169246005059055),
            TradeEvent(ticker = "Ticker439", price = 95.0f, size = 49, time = -7252680086516976403),
            TradeEvent(ticker = "Ticker448", price = 70.0f, size = 36, time = -4033282910995118951),
            BidEvent(ticker = "Ticker938", price = 7.0f, size = 62, time = -6616814657806899356),
            AskEvent(ticker = "Ticker374", price = 7.0f, size = 15, time = 4731288498627745830),
            AskEvent(ticker = "Ticker853", price = 65.0f, size = 11, time = 1669441119088229790),
            AskEvent(ticker = "Ticker339", price = 80.0f, size = 29, time = -8415103301060278097),
        )
        assertEquals(expected, actual)
    }
}
