abstract class Consumer<T> {
    abstract fun consume(elem: T)
}

class Printer<T> : Consumer<T>() {
    private var toPrint: T? = null

    fun print() {
        println("Printing $toPrint")
    }

    override fun consume(elem: T) {
        toPrint = elem
    }
}

class Sender<T> : Consumer<T>() {
    override fun consume(elem: T) {
        // ...
    }
}

fun getConsumer(): Consumer<Number> = Printer()

fun sendInt(sender: Sender<Int>) {}
fun sendFloat(sender: Sender<Float>) {}

fun main(args: Array<String>) {
    val consumer = getConsumer()
    consumer.consume(10)

    val sender = Sender<Number>()
//    sendInt(sender)
//    sendFloat(sender)
//    val c1: Consumer<Int> = Printer<Number>()
//    val c2: Consumer<Int> = Sender<Number>()
//    val c3: Printer<Int> = Printer<Number>()
//    val c4: Sender<Int> = Sender<Number>()
}
