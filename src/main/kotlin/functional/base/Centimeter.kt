package functional.base.centimeter

import kotlin.math.abs

// TODO

class Centimeter(val value: Double) {
    fun plus(other: Centimeter): Centimeter =
        Centimeter(value + other.value)

    fun times(other: Double): Centimeter =
        Centimeter(value * other)

    override fun toString(): String = "$value cm"
}

val Int.cm get() = Centimeter(this.toDouble())

fun distance(from: Centimeter, to: Centimeter): Centimeter =
    Centimeter(abs(to.value - from.value))

fun main() {
    val f1 = Centimeter::plus
    val f2 = Centimeter::times
    val f3 = Centimeter::value
    val f4 = Centimeter::toString
    val f5 = Centimeter(1.0)::plus
    val f6 = Centimeter(2.0)::times
    val f7 = Centimeter(3.0)::value
    val f8 = Centimeter(4.0)::toString
    val f9 = Int::cm
    val f10 = 123::cm
    val f11 = ::distance
}
