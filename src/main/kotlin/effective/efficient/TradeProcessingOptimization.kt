package effective.efficient

import effective.efficient.Filter.*
import effective.efficient.Filter.Relation.*
import effective.efficient.Filter.SnapshotPart.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.io.DataInputStream
import java.io.File
import java.util.concurrent.ConcurrentHashMap
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
    val price: Price,
    val size: Int?,
    val time: Long?,
)

data class Ticker(val value: Int)
data class Price(val value: Double?)

sealed interface Event {
    val ticker: Int
}

data class BidEvent(override val ticker: Int, val price: Double?, val size: Int?, val time: Long?) : Event
data class AskEvent(override val ticker: Int, val price: Double?, val size: Int?, val time: Long?) : Event
data class TradeEvent(override val ticker: Int, val price: Double?, val size: Int?, val time: Long?) : Event

class MarketClient {
    private val file = File("market.txt")

    fun observe(): Flow<Event> = flow {
        val input = DataInputStream(file.inputStream())
        while (true) {
            val event = when (input.read()) {
                0 -> BidEvent(
                    input.readInt(),
                    input.readDouble(),
                    input.readInt(),
                    input.readLong(),
                )

                1 -> AskEvent(
                    input.readInt(),
                    input.readDouble(),
                    input.readInt(),
                    input.readLong(),
                )

                2 -> TradeEvent(
                    input.readInt(),
                    input.readDouble(),
                    input.readInt(),
                    input.readLong(),
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
) {
    private val snapshots = ConcurrentHashMap<Ticker, Snapshot>()

    fun observeUpdates() = client.observe()
        .map { update ->
            val snapshot = when (update) {
                is BidEvent -> {
                    snapshots.getOrPut(Ticker(update.ticker)) { Snapshot(null, null, null) }
                        .copy(bid = PriceSizeTime(Price(update.price), update.size, update.time))
                }

                is AskEvent -> {
                    snapshots.getOrPut(Ticker(update.ticker)) { Snapshot(null, null, null) }
                        .copy(ask = PriceSizeTime(Price(update.price), update.size, update.time))
                }

                is TradeEvent -> {
                    snapshots.getOrPut(Ticker(update.ticker)) { Snapshot(null, null, null) }
                        .copy(last = PriceSizeTime(Price(update.price), update.size, update.time))
                }
            }
            snapshots[Ticker(update.ticker)] = snapshot
            TickerSnapshot(Ticker(update.ticker), snapshot)
        }
        .onStart { snapshots.forEach { emit(TickerSnapshot(it.key, it.value)) } }
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
            And(listOf(TickerIs(listOf(Ticker(0), Ticker(1))), PrizeCondition(Ask, GreaterThan, 99000.0))),
            And(listOf(PrizeCondition(Spread, GreaterThan, 99000.0))),
        )
    )

    val timedValue = measureTimedValue {
        service.observeUpdates(
            filter = filter,
            tickers = List(70) { it }
        ).take(50)
            .onEach { println(it) }
            .toList()
    }
    println("Took ${timedValue.duration}")
//    val expected = listOf(
//    )
//    if (timedValue.value != expected) {
//        println("The result is incorrect")
//    } else {
//        println("The result is correct")
//    }
}
// 22.18 s
// 2.91 GB
