package examples

import kotlin.random.Random

fun a() {}


fun main() {
    val answer = if (testHasPassed()) 42 else -1

    val value = valueOrNull() ?: "default value"
    
    val message = when {
        option1() -> "option1"
        option2() -> "option2"
        option3() -> "option3"
        option4() -> "option4"
        else -> "default"
    }
}

fun testHasPassed(): Boolean = Random.nextBoolean()
fun valueOrNull(): String? = if (Random.nextBoolean()) "value" else null
fun option1(): Boolean = Random.nextBoolean()
fun option2(): Boolean = Random.nextBoolean()
fun option3(): Boolean = Random.nextBoolean()
fun option4(): Boolean = Random.nextBoolean()
