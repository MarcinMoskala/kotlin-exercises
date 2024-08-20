package examples.essentials

interface TradeListener {
    fun onTrade(trade: Trade)
    fun onAsk(ask: Ask)
    fun onBid(bid: Bid)
}

class TradeListenerImpl : TradeListener {
    override fun onTrade(trade: Trade) {
        println("Trade: ${trade.price} ${trade.volume}")
    }

    override fun onAsk(ask: Ask) {
        println("Ask: ${ask.price} ${ask.volume}")
    }

    override fun onBid(bid: Bid) {
        println("Bid: ${bid.price} ${bid.volume}")
    }
}

fun main() {
    val tradeObserver = TradeObserver()
    tradeObserver.addListener(TradeListenerImpl())

    tradeObserver.notifyTrade(Trade(100.0, 10.0))
    tradeObserver.notifyAsk(Ask(80.0, 5.0))
    tradeObserver.notifyBid(Bid(120.0, 15.0))
}

class TradeObserver {
    private val listeners = mutableListOf<TradeListener>()

    fun addListener(listener: TradeListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: TradeListener) {
        listeners.remove(listener)
    }

    fun notifyTrade(trade: Trade) {
        listeners.forEach { it.onTrade(trade) }
    }

    fun notifyAsk(ask: Ask) {
        listeners.forEach { it.onAsk(ask) }
    }

    fun notifyBid(bid: Bid) {
        listeners.forEach { it.onBid(bid) }
    }
}

class Trade(val price: Double, val volume: Double)
class Ask(val price: Double, val volume: Double)
class Bid(val price: Double, val volume: Double)
