import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode.HALF_EVEN

abstract class Money(
    val amount: BigDecimal,
    val currency: String
)

fun main() {
    val eur: EUR = EUR.from("12.00")
    val pln: PLN = PLN.from(20)
    val usd: USD = USD.from(32.5)
}

class USD(amount: BigDecimal) : Money(amount, "USD") {
    companion object {
        private val MATH = MathContext(2, HALF_EVEN)
        fun from(amount: Int): USD = USD(amount.toBigDecimal(MATH))
        fun from(amount: Double): USD = USD(amount.toBigDecimal(MATH))
        fun from(amount: String): USD = USD(amount.toBigDecimal(MATH))
    }
}

class EUR(amount: BigDecimal) : Money(amount, "EUR") {
    companion object {
        private val MATH = MathContext(2, HALF_EVEN)
        fun from(amount: Int): EUR = EUR(amount.toBigDecimal(MATH))
        fun from(amount: Double): EUR = EUR(amount.toBigDecimal(MATH))
        fun from(amount: String): EUR = EUR(amount.toBigDecimal(MATH))
    }
}

class PLN(amount: BigDecimal) : Money(amount, "PLN") {
    companion object {
        private val MATH = MathContext(2, HALF_EVEN)
        fun from(amount: Int): PLN = PLN(amount.toBigDecimal(MATH))
        fun from(amount: Double): PLN = PLN(amount.toBigDecimal(MATH))
        fun from(amount: String): PLN = PLN(amount.toBigDecimal(MATH))
    }
}

//abstract class MoneyMaker<M : Money> {
//    private val MATH = MathContext(2, HALF_EVEN)
//    abstract fun from(amount: BigDecimal): M
//    fun from(amount: Int): M = from(amount.toBigDecimal(MATH))
//    fun from(amount: Double): M = from(amount.toBigDecimal(MATH))
//    fun from(amount: String): M = from(amount.toBigDecimal(MATH))
//}
//
//class USD(amount: BigDecimal) : Money(amount, "USD") {
//    companion object : MoneyMaker<USD>() {
//        override fun from(amount: BigDecimal): USD = USD(amount)
//    }
//}
//
//class EUR(amount: BigDecimal) : Money(amount, "EUR") {
//    companion object : MoneyMaker<EUR>() {
//        override fun from(amount: BigDecimal): EUR = EUR(amount)
//    }
//}
//
//class PLN(amount: BigDecimal) : Money(amount, "PLN") {
//    companion object : MoneyMaker<PLN>() {
//        override fun from(amount: BigDecimal): PLN = PLN(amount)
//    }
//}
