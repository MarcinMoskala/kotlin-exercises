package examples.coroutines.underthehood.udh0

import kotlinx.coroutines.delay

suspend fun myFunction() {
    println("Before")
    delay(1000) // suspending
    println("After")
}

suspend fun main() {
    println("Before")
    myFunction()
    println("After")
}