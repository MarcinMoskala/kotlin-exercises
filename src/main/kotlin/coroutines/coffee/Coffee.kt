package coffee

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

data class Order(val customer: String, val type: CoffeeType)
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

suspend fun main() = coroutineScope<Unit> {
    val orders = List(100) { Order("Customer$it", CoffeeType.entries.random()) }
    val startTime = System.currentTimeMillis()

    serveOrders(orders) { coffee, customer, barista ->
        println("Coffee $coffee for $customer made by $barista")
    }

    val endTime = System.currentTimeMillis()
    println("Done, took ${endTime - startTime}")
}

suspend fun serveOrders(orders: List<Order>, serveCoffee: (coffee: Coffee, customer: String, barista: String) -> Unit) =
    coroutineScope {
        for (order in orders) {
            launch {
                val groundCoffee = groundCoffee()
                val espresso = makeEspresso(groundCoffee)
                val coffee = when (order.type) {
                    CoffeeType.ESPRESSO -> espresso
                    CoffeeType.LATE -> {
                        val milk = brewMilk()
                        Latte(milk, espresso)
                    }
                }
                serveCoffee(coffee, order.customer, "Bob")
            }
        }
    }

fun groundCoffee(): GroundCoffee {
    longOperation()
    return GroundCoffee()
}

fun brewMilk(): Milk {
    longOperation()
    return Milk()
}


fun makeEspresso(ground: GroundCoffee): Espresso {
    longOperation()
    return Espresso(ground)
}

class EspressoMachine {
    fun makeEspresso(ground: GroundCoffee): Espresso = synchronized(this) {
        Thread.sleep(1000)
        return Espresso(ground)
    }
}

fun longOperation() {
//    val size = 820 // ~1 second on my MacBook
    val size = 350 // ~0.1 second on my MacBook
    val list = List(size) { it }
    val listOfLists = List(size) { list }
    val listOfListsOfLists = List(size) { listOfLists }
    listOfListsOfLists.hashCode()
//    Thread.sleep(1000)
}
