package essentials.operators

import org.junit.Test
import utils.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals

data class Money(
    val amount: BigDecimal,
    val currency: Currency,
) {
    companion object {
        fun eur(amount: String) = Money(BigDecimal(amount), Currency.EUR)
    }
}

enum class Currency {
    EUR, USD, GBP
}

fun main() {
    val money1 = Money.eur("10.00")
    val money2 = Money.eur("29.99")

//    println(money1 + money2) // Money(amount=39.99, currency=EUR)
//    println(money2 - money1) // Money(amount=19.99, currency=EUR)
//    println(-money1) // Money(amount=-10.00, currency=EUR)
//    println(money1 * 3) // Money(amount=30.00, currency=EUR)
}

class MoneyOperationsTest {
//    @Test
//    fun `Money can be added`() {
//        val money1 = Money.eur("10.00")
//        val money2 = Money.eur("29.99")
//        assertEquals(Money.eur("39.99"), money1 + money2)
//    }
//
//    @Test
//    fun `Money throws exception when adding different currencies`() {
//        for (currency in Currency.entries) {
//            for (otherCurrency in (Currency.entries - currency)) {
//                val money1 = Money(BigDecimal("10.00"), currency)
//                val money2 = Money(BigDecimal("29.99"), otherCurrency)
//                assertThrows<IllegalArgumentException> { money1 + money2 }
//            }
//        }
//    }
//
//    @Test
//    fun `Money can be subtracted`() {
//        val money1 = Money.eur("10.00")
//        val money2 = Money.eur("29.99")
//        assertEquals(Money.eur("19.99"), money2 - money1)
//    }
//
//    @Test
//    fun `Money throws exception when subtracting different currencies`() {
//        for (currency in Currency.entries) {
//            for (otherCurrency in (Currency.entries - currency)) {
//                val money1 = Money(BigDecimal("10.00"), currency)
//                val money2 = Money(BigDecimal("29.99"), otherCurrency)
//                assertThrows<IllegalArgumentException> { money1 - money2 }
//            }
//        }
//    }
//
//    @Test
//    fun `Money can be negated`() {
//        assertEquals(Money.eur("-10.00"), -Money.eur("10.00"))
//        assertEquals(Money.eur("-100.00"), -Money.eur("100.00"))
//        assertEquals(Money.eur("10.00"), -Money.eur("-10.00"))
//        assertEquals(Money.eur("-999.999"), -Money.eur("999.999"))
//
//    }
//
//    @Test
//    fun `Money can be multiplied by a number`() {
//        assertEquals(Money.eur("30.00"), Money.eur("10.00") * 3)
//        assertEquals(Money.eur("0.00"), Money.eur("10.00") * 0)
//        assertEquals(Money.eur("-30.00"), Money.eur("10.00") * -3)
//    }
}
