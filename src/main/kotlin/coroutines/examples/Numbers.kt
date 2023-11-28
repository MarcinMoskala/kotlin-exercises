package examples

val childNumbers = sequence {
    println("Um, first number is... one!")
    yield(1)

    println("Next is... eeeee two!")
    yield(2)

    println("twotwotwo... ummmm three!")
    yield(3)

    println("That's all I've learned")
}

fun main() {
    val iterator = childNumbers.iterator()
    println("What is first?")
    println("Yes, it is ${iterator.next()}")
    println("What is next?")
    println("Good, it is ${iterator.next()}")
}