package advanced.java

import java.math.BigDecimal

data class Money(
    val amount: BigDecimal = BigDecimal.ZERO,
    val currency: Currency = Currency.EUR,
) {
    companion object {
        fun eur(amount: String) =
            Money(BigDecimal(amount), Currency.EUR)

        fun usd(amount: String) =
            Money(BigDecimal(amount), Currency.USD)

        val ZERO_EUR = eur("0.00")
    }
}

fun List<Money>.sum(): Money? {
    if (isEmpty()) return null
    val currency = this.map { it.currency }.toSet().single()
    return Money(
        amount = sumOf { it.amount },
        currency = currency
    )
}

operator fun Money.plus(other: Money): Money {
    require(currency == other.currency)
    return Money(amount + other.amount, currency)
}

enum class Currency {
    EUR, USD
}

fun main() {
    val money1 = Money.eur("10.00")
    val money2 = Money.eur("29.99")

    println(listOf(money1, money2, money1).sum())
    // Money(amount=49.99, currency=EUR)

    println(money1 + money2)
    // Money(amount=39.99, currency=EUR)

    val money3 = Money.usd("10.00")
    val money4 = Money()
    val money5 = Money(BigDecimal.ONE)
    val money6 = Money.ZERO_EUR
}
