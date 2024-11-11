package effective.efficient

import effective.efficient.Filter.*
import effective.efficient.Filter.Relation.*
import effective.efficient.Filter.SnapshotPart.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random
import kotlin.time.measureTimedValue

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
    val price: Price?,
    val size: Int?,
    val time: Long?,
)

data class Ticker(val value: Int?)
data class Price(val value: Double?)

sealed interface Event {
    val ticker: Int?
}

data class BidEvent(override val ticker: Int?, val price: Double?, val size: Int?, val time: Long?) : Event
data class AskEvent(override val ticker: Int?, val price: Double?, val size: Int?, val time: Long?) : Event
data class TradeEvent(override val ticker: Int?, val price: Double?, val size: Int?, val time: Long?) : Event

class MarketClient {
    private val random = Random(42)

    fun observe(): Flow<Event> = flow {
        while (true) {
            val event = when (random.nextInt(3)) {
                0 -> BidEvent(
                    random.nextInt(1000),
                    random.nextDouble(100_000.0),
                    random.nextInt(100_000),
                    random.nextLong(),
                )

                1 -> AskEvent(
                    random.nextInt(1000),
                    random.nextDouble(100_000.0),
                    random.nextInt(100_000),
                    random.nextLong(),
                )

                2 -> TradeEvent(
                    random.nextInt(1000),
                    random.nextDouble(100_000.0),
                    random.nextInt(100_000),
                    random.nextLong(),
                )

                else -> throw IllegalArgumentException()
            }
            emit(event)
        }
    }
}

class MarketRepository(
    private val client: MarketClient,
) {
    private val snapshots = HashMap<Ticker, Snapshot>()

    fun observeUpdates() = client.observe()
        .map { update ->
            val ticker = Ticker(update.ticker)
            val snapshot = snapshots.getOrPut(ticker) { Snapshot(null, null, null) }
                .withUpdate(update)
            snapshots[ticker] = snapshot
            TickerSnapshot(ticker, snapshot)
        }
}

fun Snapshot.withUpdate(event: Event): Snapshot = when (event) {
    is BidEvent -> copy(bid = PriceSizeTime(Price(event.price), event.size, event.time))
    is AskEvent -> copy(ask = PriceSizeTime(Price(event.price), event.size, event.time))
    is TradeEvent -> copy(last = PriceSizeTime(Price(event.price), event.size, event.time))
}

// No need to touch this class
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
        tickers: List<Int>? = null,
    ) = repository.observeUpdates()
        .filter { tickers == null || it.ticker.value in tickers }
        .filter { filter == null || filter.check(it) }
}

suspend fun main() {
    val client = MarketClient()
    val repository = MarketRepository(client)
    val service = TradeService(repository)
    val filter = Or(
        listOf(
            And(listOf(TickerIs(List(40) { Ticker(it) }), PrizeCondition(Ask, GreaterThan, 99999.0))),
            And(listOf(PrizeCondition(Spread, GreaterThan, 99950.0))),
        )
    )

    val timedValue = measureTimedValue {
        service.observeUpdates(
            filter = filter,
            tickers = List(70) { it }
        ).take(50)
            .onEach { println(it) }
            .toList()
            .map { it.ticker.value }
    }
    println("Took ${timedValue.duration}")
    val expected = listOf(
        20, 20, 20, 20, 20, 20, 20, 22, 15, 15, 15, 15, 13, 4, 4, 4, 27, 54, 
        54, 23, 23, 23, 23, 23, 23, 23, 18, 18, 18, 25, 25, 27, 32, 32, 7, 7, 
        7, 17, 17, 17, 17, 17, 28, 28, 29, 29, 63, 21, 21, 21
    )
    if (timedValue.value != expected) {
        println("The result is incorrect\nExpected: $expected\nActual: ${timedValue.value}")
    } else {
        println("The result is correct")
    }
}
