package examples.essentials.exceptions

private fun calculate(): Int {
    val result = 1 / 0
    println("Calculated")
    return result
}

private fun printCalculated() {
    val calculate = calculate()
    println(calculate)
}

fun main() {
    println("Before")
    printCalculated()
    println("After")
}

//class CustomException(message: String) : Exception(message)
//
//private fun functionThrowing() {
//    throw CustomException("Some message")
//}
//
//fun main() {
//    println("Before")
//    functionThrowing()
//    println("After")
//}
