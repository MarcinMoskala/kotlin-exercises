package essentials.operators

import java.math.BigDecimal

data class Money(
    val amount: BigDecimal,
    val currency: Currency
) {
    operator fun plus(other: Money): Money {
        require(currency == other.currency) {
            "Cannot add money of different currencies"
        }
        return Money(amount + other.amount, currency)
    }
    
    operator fun minus(other: Money): Money {
        require(currency == other.currency) {
            "Cannot subtract money of different currencies"
        }
        return Money(amount - other.amount, currency)
    }
    
    operator fun unaryMinus(): Money = 
        Money(-amount, currency)
    
    operator fun times(times: Int): Money = 
        Money(amount * times.toBigDecimal(), currency)
    
    companion object {
        fun eur(amount: String) = 
            Money(BigDecimal(amount), Currency.EUR)
    }
}


enum class Currency {
    EUR, USD, GBP, RUB
}

fun main() {
    val money1 = Money.eur("10.00")
    val money2 = Money.eur("29.99")

    println(money1 + money2) // Money(amount=39.99, currency=EUR)
    println(money2 - money1) // Money(amount=19.99, currency=EUR)
    println(-money1) // Money(amount=-10.00, currency=EUR)
    println(money1 * 3) // Money(amount=30.00, currency=EUR)
}
