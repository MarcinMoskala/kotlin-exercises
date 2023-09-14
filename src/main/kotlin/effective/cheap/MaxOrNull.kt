package cheap

import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun Iterable<Int>.maxOrNullOld(): Int? {
    var max: Int? = null
    for (i in this) {
        if (i > max ?: Int.MIN_VALUE) {
            max = i
        }
    }
    return max
}

fun Iterable<Int>.maxOrNull(): Int? {
    TODO()
}


fun main() {
    val random = Random(123456789)
    val list = List(10) { List(A_LOT) { random.nextInt(A_LOT) } }
    measureTimeMillis {
        println(list.map { it.maxOrNullOld()!! }.maxOrNullOld())
    }.let(::println)
    measureTimeMillis {
        println(list.map { it.maxOrNull()!! }.maxOrNull())
    }.let(::println)
}

const val A_LOT = 1_000_000