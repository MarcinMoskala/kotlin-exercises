package effective.efficient

import effective.efficient.Filter.*
import effective.efficient.Filter.Relation.*
import effective.efficient.Filter.SnapshotPart.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.DataInputStream
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

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
data class Price(val value: Double?)

sealed interface Event {
    val ticker: String
}

data class BidEvent(override val ticker: String, val price: Double?, val size: Int?, val time: Long?) : Event
data class AskEvent(override val ticker: String, val price: Double?, val size: Int?, val time: Long?) : Event
data class TradeEvent(override val ticker: String, val price: Double?, val size: Int?, val time: Long?) : Event


class MarketClient {
    private val file = File("market.txt")

    fun observe(): Flow<Event> = flow {
        val input = DataInputStream(file.inputStream())
        var i = 0
        while (true) {
            val event = when (input.read()) {
                0 -> BidEvent(
                    input.readText(10).trim(),
                    input.readDouble().takeUnless { it.isNaN() },
                    input.readInt().takeUnless { it == -1 },
                    input.readLong().takeUnless { it == -1L },
                )

                1 -> AskEvent(
                    input.readText(10).trim(),
                    input.readDouble().takeUnless { it.isNaN() },
                    input.readInt().takeUnless { it == -1 },
                    input.readLong().takeUnless { it == -1L },
                )

                2 -> TradeEvent(
                    input.readText(10).trim(),
                    input.readDouble().takeUnless { it.isNaN() },
                    input.readInt().takeUnless { it == -1 },
                    input.readLong().takeUnless { it == -1L },
                )
                
                -1 -> {
                    println("End of file")
                    break
                }

                else -> throw IllegalArgumentException()
            }
            emit(event)
        }
    }.flowOn(Dispatchers.IO)
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
        val value: Double,
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
            And(listOf(TickerIs(tickers.take(1).map(::Ticker)), PrizeCondition(Ask, GreaterThan, 99000.0))),
            And(listOf(PrizeCondition(Spread, GreaterThan, 99000.0))),
        )
    )

    measureTimeMillis {
        service.observeUpdates(
            filter = filter,
            tickers = tickers.take(70)
        ).take(50)
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
                And(listOf(TickerIs(tickers.take(1).map(::Ticker)), PrizeCondition(Ask, GreaterThan, 99.0))),
                And(listOf(PrizeCondition(Spread, GreaterThan, 99.0))),
            )
        )

        val result: List<TickerSnapshot> = service.observeUpdates(
            filter = filter,
            tickers = tickers.take(70)
        ).take(3)
            .toList()

        val expected = listOf<TickerSnapshot>(
            TickerSnapshot(
                ticker = Ticker(value = "Ticker2"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(price = Price(value = 42949.0), size = 14366, time = null),
                    ask = PriceSizeTime(price = Price(value = 91372.0), size = 59547, time = -2163789171097761772),
                    last = null
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker17"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(
                        price = Price(value = 3914.0),
                        size = 85765,
                        time = -3896580439955758629
                    ),
                    ask = PriceSizeTime(price = Price(value = 64165.0), size = 96267, time = 2044103532301031029),
                    last = PriceSizeTime(price = Price(value = 14907.0), size = 40464, time = -112981419869231978)
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker25"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(
                        price = Price(value = 42149.0),
                        size = 8021,
                        time = 7641730672501288957
                    ),
                    ask = PriceSizeTime(price = Price(value = 84467.0), size = 88206, time = -85254649426941972),
                    last = null
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
                        price = Price(value = 14126.0),
                        size = 20,
                        time = -7154366676655009910
                    ),
                    ask = null,
                    last = PriceSizeTime(price = Price(value = 72469.0), size = 59291, time = 8119465514165684939)
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker792"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(
                        price = Price(value = 12488.0),
                        size = 63606,
                        time = 9046446809172565568
                    ), ask = null, last = null
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker767"),
                snapshot = Snapshot(
                    bid = null,
                    ask = PriceSizeTime(price = Price(value = 36388.0), size = 72810, time = -3981946300853867545),
                    last = null
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker44"),
                snapshot = Snapshot(
                    bid = null,
                    ask = null,
                    last = PriceSizeTime(price = Price(value = 93719.0), size = 62193, time = 8928513245163532476)
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker439"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(
                        price = Price(value = 69194.0),
                        size = 30476,
                        time = 5705359404818493164
                    ),
                    ask = PriceSizeTime(price = Price(value = 37268.0), size = 52609, time = 2210899662547177636),
                    last = null
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker715"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(
                        price = Price(value = 15856.0),
                        size = 79677,
                        time = 674890693049164191
                    ),
                    ask = PriceSizeTime(price = Price(value = 52918.0), size = 17085, time = -4671874103540276010),
                    last = null
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker847"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(
                        price = Price(value = 204.0),
                        size = 73960,
                        time = -1342033315582348433
                    ),
                    ask = PriceSizeTime(price = Price(value = 92634.0), size = 13011, time = -1458896883356733664),
                    last = null
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker530"),
                snapshot = Snapshot(
                    bid = null,
                    ask = null,
                    last = PriceSizeTime(price = Price(value = 15944.0), size = 48744, time = 5655792291667859376)
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker832"),
                snapshot = Snapshot(
                    bid = PriceSizeTime(
                        price = Price(value = 36946.0),
                        size = 29204,
                        time = 5723479612060452388
                    ), ask = null, last = null
                )
            ),
            TickerSnapshot(
                ticker = Ticker(value = "Ticker602"),
                snapshot = Snapshot(
                    bid = null,
                    ask = null,
                    last = PriceSizeTime(price = Price(value = 68222.0), size = 7445, time = 4176048430149649671)
                )
            )
        )
        assertEquals(expected, result)
    }

    @Test
    fun consistencyMarketClient() = runTest {
        val client = MarketClient()
        val actual = client.observe().drop(1000).take(8).toList()

        val expected: List<Event> = listOf<Event>(
            BidEvent(ticker = "Ticker104", price = 47706.0, size = 50371, time = -3918742120704651959),
            BidEvent(ticker = "Ticker217", price = 44324.0, size = 55025, time = -8444169246005059055),
            TradeEvent(ticker = "Ticker439", price = 74209.0, size = 67798, time = -7252680086516976403),
            TradeEvent(ticker = "Ticker448", price = 21025.0, size = 7460, time = -4033282910995118951),
            BidEvent(ticker = "Ticker938", price = 9987.0, size = 14552, time = -6616814657806899356),
            AskEvent(ticker = "Ticker374", price = 94468.0, size = 44989, time = 4731288498627745830),
            AskEvent(ticker = "Ticker853", price = 88951.0, size = 63652, time = 1669441119088229790),
            AskEvent(ticker = "Ticker339", price = 51662.0, size = 60302, time = -8415103301060278097)
        )
        assertEquals(expected, actual)
    }
}
