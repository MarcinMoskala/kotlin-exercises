package examples.essentials.ranges

import java.math.BigDecimal
import java.time.Instant

fun main() {
    val amount = BigDecimal("100.00")
    val minAmount = BigDecimal("10.00")
    val maxAmount = BigDecimal("1000.00")
    
    if (amount in minAmount..maxAmount) {
        println("Amount is in range")
    } else {
        println("Amount is out of range")
    }
    
    
    val now = Instant.now()
    val actionStart = Instant.parse("2021-01-01T00:00:00Z")
    val actionEnd = Instant.parse("2024-11-02T00:00:00Z")

    if (now in actionStart..actionEnd) {
        println("Action is in progress")
    } else {
        println("Action is not in progress")
    }
}

