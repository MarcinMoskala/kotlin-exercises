package functional

infix fun <A, B, C> ((A) -> B).then(f: (B) -> C): (A) -> C = { f(this(it)) }
infix fun ((Int) -> Int).thenAdd(value: Int): (Int) -> Int = { this(it) + value }

fun main() {
    val double = { i: Int -> i * 2 }
    val triple = { i: Int -> i * 3 }
    val square = { i: Int -> i * i }
    val function = double then triple then square thenAdd 6
    println(function(1))
}
