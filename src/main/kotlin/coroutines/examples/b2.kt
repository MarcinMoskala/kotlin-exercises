import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        delay(1000L)
        println("World!")
    }
    runBlocking {
        delay(1000L)
        println("World!")
    }
    runBlocking {
        delay(1000L)
        println("World!")
    }
    println("Hello,")
}

//fun main() {
//    Thread.sleep(1000L)
//    println("World!")
//    Thread.sleep(1000L)
//    println("World!")
//    Thread.sleep(1000L)
//    println("World!")
//    println("Hello,")
//}
