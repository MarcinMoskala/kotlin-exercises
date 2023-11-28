import kotlinx.coroutines.*
import kotlin.concurrent.thread

fun main() {
    val value1 = GlobalScope.async {
        delay(1000L)
        1
    }
    val value2 = GlobalScope.async {
        delay(1000L)
        20
    }
    val value3 = GlobalScope.async {
        delay(1000L)
        300
    }
    println("Calculating")
    runBlocking {
        print(value1.await() + value2.await() + value3.await())
    }
}

//fun main() {
//    thread(isDaemon = true) {
//        Thread.sleep(1000L)
//        println("World!")
//    }
//    thread(isDaemon = true) {
//        Thread.sleep(1000L)
//        println("World!")
//    }
//    thread(isDaemon = true) {
//        Thread.sleep(1000L)
//        println("World!")
//    }
//    println("Hello,")
//    Thread.sleep(2000L)
//}
