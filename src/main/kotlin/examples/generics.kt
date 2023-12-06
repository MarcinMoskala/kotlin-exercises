@file:Suppress("unused")

package examples.generics

import examples.generics.ScheduledTaskUpdate.ChangeTo

interface Animal {
    fun pet()
}
class Cat(val name: String): Animal {
    override fun pet() {
        print("Meaw")
    }
}
class Dog(val name: String): Animal {
    override fun pet() {
        print("Waff")
    }
}

fun petAnimals(animals: List<Animal>) {
    for (animal in animals) {
        animal.pet()
    }
}

//fun main() {
//    val cats: List<Cat> = listOf(Cat("Mruczek"), Cat("Puszek"))
//    petAnimals(cats)
//}

private const val ORDER_MANAGER_URL = "http://localhost:8080/order-manager"
private const val INVOICE_MANAGER_URL = "http://localhost:8080/order-manager"

class Order
class Connection(val url: String) {
    fun send(message: Message) {
        println("Sending message $message to $url")
    }
}

interface Message

interface OrderManagerMessage : Message
class AddOrder(val order: Order) : OrderManagerMessage
class CancelOrder(val orderId: String) : OrderManagerMessage

interface InvoiceManagerMessage : Message
class MakeInvoice(val order: Order) : OrderManagerMessage

class GeneralSender(serviceUrl: String) : Sender<Message> {
    private val connection = Connection(serviceUrl)

    override fun send(message: Message) {
        connection.send(message)
    }
}

interface Sender<T : Message> {
    fun send(message: T)
}

//val orderManagerSender: Sender<OrderManagerMessage> =
//    GeneralSender(ORDER_MANAGER_URL)
//
//val invoiceManagerSender: Sender<InvoiceManagerMessage> =
//    GeneralSender(INVOICE_MANAGER_URL)

interface ScannerMessage
class ChangeScannerMode(val mode: String) : ScannerMessage
class StopScanning : ScannerMessage

fun getNumbers() = listOf(1, 2, 3)

fun printEachNumber(transformation: (Int)->Any) {
   val numbers: List<Int> = getNumbers()
   for(num in numbers) print(transformation(num))
}

//fun main() {
//    val double: (Int) -> Int = { it * 2 }
//    printEachNumber(double)
//
//    val toDouble: (Number)->Double = Number::toDouble
//    printEachNumber(toDouble)
//
//    val toString: (Any) -> String = Any::toString
//    printEachNumber(toString)
//}

sealed interface LinkedList<T>
data class Node<T>(val value: T, val next: LinkedList<T>) : LinkedList<T>
class Empty<T> : LinkedList<T>

//fun main() {
//    val list2: LinkedList<String> = Empty
//    val list: LinkedList<Int> = Node(1, Node(2, Node(3, Empty)))
//    println(list)
//    
//    val l1 = listOf<Int>()
//    val l2 = emptyList<String>()
//    println(l1 === l2)
//}

//fun main() {
//    val list = listOf<Int>()
//}

class ScheduledTask(
    val name: String,
    val data: Any?,
    val scheduledAt: Long,
    val repeatEvery: Long,
    // ...
)

class ScheduledTaskUpdate(
    val name: PropertyUpdate<String> = Keep,
    val data: PropertyUpdate<Any?> = Keep,
    val scheduledAt: PropertyUpdate<Long> = Keep,
    val repeatEvery: PropertyUpdate<Long> = Keep,
    // ... 
) {
    sealed interface PropertyUpdate<out T>
    object Keep: PropertyUpdate<Nothing>
    class ChangeTo<T>(val value: T): PropertyUpdate<T>
}

//fun main() {
//    val update = ScheduledTaskUpdate(
//        name = ChangeTo("New name"),
//        data = ChangeTo(null),
//    )
//}

sealed class Either<L, R>
class Left<L, R>(val value: L) : Either<L, R>()
class Right<L, R>(val value: R) : Either<L, R>()

//fun main() {
//    val leftError: Left<Error> = Left(Error())
//    val leftThrowable: Left<Throwable> = leftError
//    val leftAny: Left<Any> = leftThrowable
//
//    val rightInt = Right(123)
//    val rightNumber: Right<Number> = rightInt
//    val rightAny: Right<Any> = rightNumber
//
//    val el: Either<Error, Int> = leftError
//    val er: Either<Error, Int> = rightInt
//
//    val etnl: Either<Throwable, Number> = leftError
//    val etnr: Either<Throwable, Number> = rightInt
//}

//class Box<T> {
//    var value: T? = null
//
//    fun put(value: T) {
//        this.value = value
//    }
//
//    fun get(): T? = value
//}

//fun <T> a(v: T): T & Any = v ?: throw Exception()
//
//fun main() {
//    val s: String = a<String>()
//    val s2: String = a<String?>()
//}


//class A<T> {
//    val t: T? = null
//}
