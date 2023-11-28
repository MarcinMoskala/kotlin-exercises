package coffee.dispatchers

import kotlinx.coroutines.*
import java.util.concurrent.Executors

val dispatcher = Dispatchers.IO.limitedParallelism(1)
//val dispatcher = Dispatchers.Default
//val dispatcher = Dispatchers.IO
//val dispatcher = Dispatchers.IO.limitedParallelism(100)

val longOperation = ::cpu1
//val longOperation = ::blocking
//val longOperation = ::suspending

fun cpu1() {
    var i = Int.MAX_VALUE / 10
    while (i > 0) {
        i -= if (i % 2 == 0) 1 else 2
    }
}

fun blocking() {
    Thread.sleep(1000)
}

suspend fun suspending() {
    delay(1000)
}

suspend fun serveOrders(orders: List<Order>) =
    coroutineScope {
        for (order in orders) {
            launch(dispatcher) {
                val coffee = makeCoffee(order)
                println("Coffee $coffee for ${order.customer} made by ${Thread.currentThread().name}")
            }
        }
    }

suspend fun main() = withContext(dispatcher) {
    val orders = List(100) { Order("Customer$it", CoffeeType.values().random()) }
    val startTime = System.currentTimeMillis()

    serveOrders(orders)

    val endTime = System.currentTimeMillis()
    println("Done, took ${endTime - startTime}")
}

private suspend fun makeCoffee(order: Order): Coffee {
    val groundCoffee = groundCoffee()
    val espresso = makeEspresso(groundCoffee)
    val coffee = when (order.type) {
        CoffeeType.ESPRESSO -> espresso
        CoffeeType.LATE -> {
            val milk = brewMilk()
            Latte(milk, espresso)
        }
    }
    return coffee
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