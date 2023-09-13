package essentials.operators

import java.math.BigDecimal

data class Money(
    val amount: BigDecimal, 
    val currency: Currency
) {
    // TODO: Implement operators overloading here
    
    companion object {
        fun eur(amount: String) = Money(BigDecimal(amount), Currency.EUR)
    }
}

enum class Currency {
    EUR, USD, GBP, RUB
}

fun main() {
    val money1 = Money.eur("10.00")
    val money2 = Money.eur("29.99")

//    println(money1 + money2) // Money(amount=39.99, currency=EUR)
//    println(money2 - money1) // Money(amount=19.99, currency=EUR)
//    println(-money1) // Money(amount=-10.00, currency=EUR)
//    println(money1 * 3) // Money(amount=30.00, currency=EUR)
}
