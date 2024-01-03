package advanced.generics.consumer

abstract class Consumer<T> {
    abstract fun consume(elem: T)
}

class Printer<T> : Consumer<T>() {
    override fun consume(elem: T) {
        // ...
    }
}

class Sender<T> : Consumer<T>() {
    override fun consume(elem: T) {
        // ...
    }
}

fun main(args: Array<String>) {
//    val p1 = Printer<Number>()
//    val p2: Printer<Int> = p1
//    val p3: Printer<Double> = p1
//
//    val s1 = Sender<Any>()
//    val s2: Sender<Int> = s1
//    val s3: Sender<String> = s1
//
//    val c1: Consumer<Number> = p1
//    val c2: Consumer<Int> = p1
//    val c3: Consumer<Double> = p1
}
