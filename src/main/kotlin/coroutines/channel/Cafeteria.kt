package coroutines.channel.cafeteria

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class CoffeeType { ESPRESSO, LATE }
class Milk
class GroundCoffee

sealed class Coffee

class Espresso(ground: GroundCoffee) : Coffee() {
    override fun toString(): String = "Espresso"
}

class Latte(milk: Milk, espresso: Espresso) : Coffee() {
    override fun toString(): String = "Latte"
}

suspend fun main() = coroutineScope {
    // TODO

    println("Hello in Dream Coffee!")
    println("Press E to get espresso, L to get late.")
    while (true) {
        val type = when (readlnOrNull()) {
            "E" -> CoffeeType.ESPRESSO
            "L" -> CoffeeType.LATE
            else -> continue
        }
        // TODO
        println("Order for $type sent")
    }
}

private suspend fun makeCoffee(order: CoffeeType, baristaName: String): Coffee {
    val groundCoffee = groundCoffee(baristaName)
    val espresso = makeEspresso(groundCoffee, baristaName)
    return when (order) {
        CoffeeType.ESPRESSO -> espresso
        CoffeeType.LATE -> {
            val milk = brewMilk(baristaName)
            Latte(milk, espresso)
        }
    }
}

suspend fun groundCoffee(baristaName: String): GroundCoffee {
    println("$baristaName: Grinding coffee...")
    delay(3000)
    return GroundCoffee()
}

suspend fun brewMilk(baristaName: String): Milk {
    println("$baristaName: Brewing milk...")
    delay(3000)
    return Milk()
}


suspend fun makeEspresso(ground: GroundCoffee, baristaName: String): Espresso {
    println("$baristaName: Making espresso...")
    delay(3000)
    return Espresso(ground)
}
