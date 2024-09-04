package examples.essentials.enum

enum class PaymentOption {
    CASH,
    CARD,
    TRANSFER,
}

fun printOption(option: PaymentOption) {
    println(option)
}

fun main() {
    val option: PaymentOption = PaymentOption.CARD
    println(option) // CARD
    printOption(option) // CARD
    println(option.name) // CARD
    println(option.ordinal) // 1

    val o: PaymentOption = PaymentOption.valueOf("TRANSFER")
//    val o: PaymentOption = enumValueOf<PaymentOption>("TRANSFER")
    println(o) // TRANSFER

    val paymentOptions: List<PaymentOption> = PaymentOption.entries
//    val paymentOptions: Array<PaymentOption> = enumValues<PaymentOption>()
    for (paymentOption in paymentOptions) {
        println(paymentOption)
    }
    // CASH
    // CARD
    // TRANSFER


//    fun transactionFee(paymentOption: PaymentOption): Double = when (paymentOption) {
//        PaymentOption.CASH -> 0.0
//        PaymentOption.CARD, PaymentOption.TRANSFER -> 0.05
//    }
//    
//    println(transactionFee(PaymentOption.CASH)) // 0.0
//    println(transactionFee(PaymentOption.CARD)) // 0.05
//    println(transactionFee(PaymentOption.TRANSFER)) // 0.05
    
    
}
