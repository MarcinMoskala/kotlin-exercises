package coffee.channel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope

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

suspend fun main() = coroutineScope {
    val ordersList = List(100) { Order("Customer$it", CoffeeType.values().random()) }
    val orders = TODO()
    val startTime = System.currentTimeMillis()

    serveOrders(orders, "Alice")
    serveOrders(orders, "Bob")
    serveOrders(orders, "Celine")
    serveOrders(orders, "Dave")

//    for ((coffee, customer, barista) in servedOrders) {
//        println("Coffee $coffee for $customer made by $barista")
//    }

    val endTime = System.currentTimeMillis()
    println("Done, took ${endTime - startTime}")
}

data class CoffeeResult(val coffee: Coffee, val customer: String, val barista: String)

fun CoroutineScope.serveOrders(orders: ReceiveChannel<Order>, baristaName: String): ReceiveChannel<CoffeeResult> = TODO()
//    val coffee = makeCoffee(order)
//    send(CoffeeResult(coffee, order.customer, baristaName))

private suspend fun makeCoffee(order: Order): Coffee {
    val groundCoffee = groundCoffee()
    val espresso = makeEspresso(groundCoffee)
    return when (order.type) {
        CoffeeType.ESPRESSO -> espresso
        CoffeeType.LATE -> {
            val milk = brewMilk()
            Latte(milk, espresso)
        }
    }
}

suspend fun groundCoffee(): GroundCoffee {
    longOperation()
    return GroundCoffee()
}

suspend fun brewMilk(): Milk {
    longOperation()
    return Milk()
}


suspend fun makeEspresso(ground: GroundCoffee): Espresso {
    longOperation()
    return Espresso(ground)
}

class EspressoMachine {
    fun makeEspresso(ground: GroundCoffee): Espresso = synchronized(this) {
        Thread.sleep(1000)
        return Espresso(ground)
    }
}

suspend fun longOperation() {
    val size = 350 // ~0.1 second on my MacBook
    val list = List(size) { it }
    val listOfLists = List(size) { list }
    val listOfListsOfLists = List(size) { listOfLists }
    listOfListsOfLists.hashCode()
//    Thread.sleep(1000)
//    delay(1000)
}