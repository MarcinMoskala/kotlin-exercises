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
            And(listOf(TickerIs(listOf(Ticker("Ticker0"))), PrizeCondition(Ask, GreaterThan, 99000.0))),
            And(listOf(PrizeCondition(Spread, GreaterThan, 99000.0))),
        )
    )

    val timedValue = measureTimedValue {
        service.observeUpdates(
            filter = filter,
            tickers = List(70) { "Ticker$it" }
        ).take(50)
            .onEach { println(it) }
            .toList()
    }
    println("Took ${timedValue.duration}")
    val expected = listOf(
        TickerSnapshot(ticker=Ticker(value="Ticker9"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=540.0), size=51697, time=6346967368062888922), ask=PriceSizeTime(price=Price(value=99997.0), size=46591, time=-2855739746868932404), last=PriceSizeTime(price=Price(value=77860.0), size=47380, time=5536083725536879369))),
        TickerSnapshot(ticker=Ticker(value="Ticker5"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=152.0), size=46554, time=-3881283181492587962), ask=PriceSizeTime(price=Price(value=99209.0), size=57224, time=7144987929939327785), last=PriceSizeTime(price=Price(value=20045.0), size=57629, time=5022239089313893571))),
        TickerSnapshot(ticker=Ticker(value="Ticker10"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=666.0), size=21008, time=5704004151645127364), ask=PriceSizeTime(price=Price(value=99872.0), size=44504, time=-3899805477748650799), last=PriceSizeTime(price=Price(value=74994.0), size=46338, time=3132576050515102034))),
        TickerSnapshot(ticker=Ticker(value="Ticker10"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=666.0), size=21008, time=5704004151645127364), ask=PriceSizeTime(price=Price(value=99872.0), size=44504, time=-3899805477748650799), last=PriceSizeTime(price=Price(value=13953.0), size=58314, time=-3293308698715133116))),
        TickerSnapshot(ticker=Ticker(value="Ticker22"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=592.0), size=55442, time=-2831176667037389238), ask=PriceSizeTime(price=Price(value=99896.0), size=33526, time=3271272601044515966), last=PriceSizeTime(price=Price(value=62770.0), size=27572, time=5936486241434963174))),
        TickerSnapshot(ticker=Ticker(value="Ticker22"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=592.0), size=55442, time=-2831176667037389238), ask=PriceSizeTime(price=Price(value=99896.0), size=33526, time=3271272601044515966), last=PriceSizeTime(price=Price(value=30892.0), size=71006, time=-713956908213918073))),
        TickerSnapshot(ticker=Ticker(value="Ticker58"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=157.0), size=45426, time=2286611379546424), ask=PriceSizeTime(price=Price(value=99276.0), size=94104, time=-4686659997649568106), last=PriceSizeTime(price=Price(value=96730.0), size=97766, time=1978902250612428951))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=33442.0), size=82857, time=8774970347844860624), ask=PriceSizeTime(price=Price(value=99803.0), size=73985, time=-3311972546936008525), last=PriceSizeTime(price=Price(value=13373.0), size=85101, time=3017813069069138670))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=48172.0), size=59320, time=-8954830625945742351), ask=PriceSizeTime(price=Price(value=99803.0), size=73985, time=-3311972546936008525), last=PriceSizeTime(price=Price(value=13373.0), size=85101, time=3017813069069138670))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=48172.0), size=59320, time=-8954830625945742351), ask=PriceSizeTime(price=Price(value=99803.0), size=73985, time=-3311972546936008525), last=PriceSizeTime(price=Price(value=43962.0), size=74227, time=-90047122419464882))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=88264.0), size=50980, time=7187509020421938398), ask=PriceSizeTime(price=Price(value=99938.0), size=32950, time=4844560259461373386), last=PriceSizeTime(price=Price(value=41149.0), size=96240, time=-6979928071605525664))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=88264.0), size=50980, time=7187509020421938398), ask=PriceSizeTime(price=Price(value=99938.0), size=32950, time=4844560259461373386), last=PriceSizeTime(price=Price(value=64496.0), size=5200, time=7955842715590592005))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=88264.0), size=50980, time=7187509020421938398), ask=PriceSizeTime(price=Price(value=99938.0), size=32950, time=4844560259461373386), last=PriceSizeTime(price=Price(value=36840.0), size=32812, time=-5120767950476111580))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=88264.0), size=50980, time=7187509020421938398), ask=PriceSizeTime(price=Price(value=99938.0), size=32950, time=4844560259461373386), last=PriceSizeTime(price=Price(value=88991.0), size=2019, time=2127658522036284949))),
        TickerSnapshot(ticker=Ticker(value="Ticker42"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=886.0), size=78497, time=-2561703668002349869), ask=PriceSizeTime(price=Price(value=99906.0), size=40985, time=-8658451656177349088), last=PriceSizeTime(price=Price(value=33596.0), size=59280, time=4785965673169044637))),
        TickerSnapshot(ticker=Ticker(value="Ticker42"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=886.0), size=78497, time=-2561703668002349869), ask=PriceSizeTime(price=Price(value=99906.0), size=40985, time=-8658451656177349088), last=PriceSizeTime(price=Price(value=23732.0), size=90590, time=-1017298203176286869))),
        TickerSnapshot(ticker=Ticker(value="Ticker44"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=427.0), size=29179, time=-2859680338743896791), ask=PriceSizeTime(price=Price(value=99507.0), size=78425, time=-2772737790593996739), last=PriceSizeTime(price=Price(value=60321.0), size=1934, time=1615163005881620641))),
        TickerSnapshot(ticker=Ticker(value="Ticker44"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=427.0), size=29179, time=-2859680338743896791), ask=PriceSizeTime(price=Price(value=99507.0), size=78425, time=-2772737790593996739), last=PriceSizeTime(price=Price(value=92634.0), size=66671, time=978741370106317863))),
        TickerSnapshot(ticker=Ticker(value="Ticker44"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=427.0), size=29179, time=-2859680338743896791), ask=PriceSizeTime(price=Price(value=99507.0), size=78425, time=-2772737790593996739), last=PriceSizeTime(price=Price(value=50424.0), size=94695, time=3253240155217318194))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=60058.0), size=26770, time=-8643744981927694862), ask=PriceSizeTime(price=Price(value=99710.0), size=46532, time=6091702250776943314), last=PriceSizeTime(price=Price(value=23759.0), size=15921, time=9162841376647023148))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=69352.0), size=46434, time=7766194492561854935), ask=PriceSizeTime(price=Price(value=99710.0), size=46532, time=6091702250776943314), last=PriceSizeTime(price=Price(value=23759.0), size=15921, time=9162841376647023148))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=52126.0), size=45324, time=1945976959290445345), ask=PriceSizeTime(price=Price(value=99710.0), size=46532, time=6091702250776943314), last=PriceSizeTime(price=Price(value=23759.0), size=15921, time=9162841376647023148))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=52126.0), size=45324, time=1945976959290445345), ask=PriceSizeTime(price=Price(value=99710.0), size=46532, time=6091702250776943314), last=PriceSizeTime(price=Price(value=68993.0), size=78377, time=5469298320912319658))),
        TickerSnapshot(ticker=Ticker(value="Ticker65"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=126.0), size=29439, time=-758230549325420645), ask=PriceSizeTime(price=Price(value=99205.0), size=13862, time=-3992141196608286466), last=PriceSizeTime(price=Price(value=69701.0), size=73495, time=-5156951188294858756))),
        TickerSnapshot(ticker=Ticker(value="Ticker65"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=126.0), size=29439, time=-758230549325420645), ask=PriceSizeTime(price=Price(value=99205.0), size=13862, time=-3992141196608286466), last=PriceSizeTime(price=Price(value=75170.0), size=21663, time=6660600883798768819))),
        TickerSnapshot(ticker=Ticker(value="Ticker65"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=126.0), size=29439, time=-758230549325420645), ask=PriceSizeTime(price=Price(value=99205.0), size=13862, time=-3992141196608286466), last=PriceSizeTime(price=Price(value=20822.0), size=67731, time=3678657481030583599))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=46076.0), size=91590, time=-6896448889112102151), ask=PriceSizeTime(price=Price(value=99456.0), size=76630, time=-5724463376747250213), last=PriceSizeTime(price=Price(value=55073.0), size=32081, time=-683007738175467130))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=49642.0), size=4311, time=8430298948699329501), ask=PriceSizeTime(price=Price(value=99456.0), size=76630, time=-5724463376747250213), last=PriceSizeTime(price=Price(value=55073.0), size=32081, time=-683007738175467130))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=49642.0), size=4311, time=8430298948699329501), ask=PriceSizeTime(price=Price(value=99456.0), size=76630, time=-5724463376747250213), last=PriceSizeTime(price=Price(value=11036.0), size=80021, time=7556694950728154718))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=49642.0), size=4311, time=8430298948699329501), ask=PriceSizeTime(price=Price(value=99456.0), size=76630, time=-5724463376747250213), last=PriceSizeTime(price=Price(value=90528.0), size=81416, time=5313870911746480468))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=57532.0), size=null, time=912244880138398797), ask=PriceSizeTime(price=Price(value=99456.0), size=76630, time=-5724463376747250213), last=PriceSizeTime(price=Price(value=90528.0), size=81416, time=5313870911746480468))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=55575.0), size=7450, time=-3906940602471196949), ask=PriceSizeTime(price=Price(value=99230.0), size=83342, time=-2492969826170268895), last=PriceSizeTime(price=Price(value=93417.0), size=79668, time=-3170972752329994248))),
        TickerSnapshot(ticker=Ticker(value="Ticker1"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=274.0), size=32502, time=-7764204936164689470), ask=PriceSizeTime(price=Price(value=99610.0), size=16599, time=7296697142540928760), last=PriceSizeTime(price=Price(value=54015.0), size=78488, time=5645960686013958314))),
        TickerSnapshot(ticker=Ticker(value="Ticker1"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=274.0), size=32502, time=-7764204936164689470), ask=PriceSizeTime(price=Price(value=99610.0), size=16599, time=7296697142540928760), last=PriceSizeTime(price=Price(value=80938.0), size=91681, time=-8760059873465304679))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=36993.0), size=72324, time=7126979630737954777), ask=PriceSizeTime(price=Price(value=99772.0), size=89981, time=6671975731050720883), last=PriceSizeTime(price=Price(value=9951.0), size=87301, time=-1662983514774541214))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=36993.0), size=72324, time=7126979630737954777), ask=PriceSizeTime(price=Price(value=99772.0), size=89981, time=6671975731050720883), last=PriceSizeTime(price=Price(value=97581.0), size=99644, time=-1331461790997419153))),
        TickerSnapshot(ticker=Ticker(value="Ticker25"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=144.0), size=33648, time=1001134990567725712), ask=PriceSizeTime(price=Price(value=99358.0), size=82400, time=4226391179237923328), last=PriceSizeTime(price=Price(value=85911.0), size=65434, time=-2612781102331639987))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=26520.0), size=26731, time=4919135318650902952), ask=PriceSizeTime(price=Price(value=99083.0), size=39978, time=-3063037908879671930), last=PriceSizeTime(price=Price(value=77388.0), size=70825, time=-6140878044941136762))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=26520.0), size=26731, time=4919135318650902952), ask=PriceSizeTime(price=Price(value=99083.0), size=39978, time=-3063037908879671930), last=PriceSizeTime(price=Price(value=53611.0), size=81284, time=424005993282040369))),
        TickerSnapshot(ticker=Ticker(value="Ticker53"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=398.0), size=41163, time=6480697380179781941), ask=PriceSizeTime(price=Price(value=99779.0), size=33382, time=6919285890995098273), last=PriceSizeTime(price=Price(value=34797.0), size=55921, time=2905373368863329016))),
        TickerSnapshot(ticker=Ticker(value="Ticker14"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=29.0), size=33007, time=98950327198230971), ask=PriceSizeTime(price=Price(value=99150.0), size=28628, time=-5753803174773506527), last=PriceSizeTime(price=Price(value=47821.0), size=42285, time=3294807416954543993))),
        TickerSnapshot(ticker=Ticker(value="Ticker42"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=472.0), size=85033, time=2494686670507371779), ask=PriceSizeTime(price=Price(value=99540.0), size=20169, time=1257981932411266195), last=PriceSizeTime(price=Price(value=41413.0), size=26173, time=-4478746252191511545))),
        TickerSnapshot(ticker=Ticker(value="Ticker42"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=472.0), size=85033, time=2494686670507371779), ask=PriceSizeTime(price=Price(value=99540.0), size=20169, time=1257981932411266195), last=PriceSizeTime(price=Price(value=21001.0), size=63252, time=-3052563536395539659))),
        TickerSnapshot(ticker=Ticker(value="Ticker42"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=472.0), size=85033, time=2494686670507371779), ask=PriceSizeTime(price=Price(value=99540.0), size=20169, time=1257981932411266195), last=PriceSizeTime(price=Price(value=97007.0), size=null, time=6002341308109964533))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=null), size=76717, time=-8511379066974690049), ask=PriceSizeTime(price=Price(value=99420.0), size=98942, time=2735528896867660577), last=PriceSizeTime(price=Price(value=57658.0), size=92026, time=1323960867990059261))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=28829.0), size=20843, time=-3167873938875712616), ask=PriceSizeTime(price=Price(value=99420.0), size=98942, time=2735528896867660577), last=PriceSizeTime(price=Price(value=57658.0), size=92026, time=1323960867990059261))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=28829.0), size=20843, time=-3167873938875712616), ask=PriceSizeTime(price=Price(value=99420.0), size=98942, time=2735528896867660577), last=PriceSizeTime(price=Price(value=49715.0), size=37920, time=null))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=43068.0), size=51012, time=-8794736234412636264), ask=PriceSizeTime(price=Price(value=99444.0), size=58399, time=5227793270467374803), last=PriceSizeTime(price=Price(value=63773.0), size=43831, time=-4896249725130559827))),
        TickerSnapshot(ticker=Ticker(value="Ticker50"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=32.0), size=26519, time=7215423036070121689), ask=PriceSizeTime(price=Price(value=99893.0), size=80834, time=8579536003987304236), last=PriceSizeTime(price=Price(value=57479.0), size=97780, time=-1405042260001534094))),
        TickerSnapshot(ticker=Ticker(value="Ticker0"), snapshot=Snapshot(bid=PriceSizeTime(price=Price(value=43577.0), size=23359, time=759447541385087383), ask=PriceSizeTime(price=Price(value=99875.0), size=64810, time=305428105014195815), last=PriceSizeTime(price=Price(value=22397.0), size=48738, time=-1064225954303373862))),
    )
    if (timedValue.value != expected) {
        println("Expected: $expected")
        println("Actual: ${timedValue.value}")
    } else {
        println("The result is correct")
    }
}
