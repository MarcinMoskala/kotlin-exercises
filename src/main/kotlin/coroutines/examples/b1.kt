import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

fun main() {
    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }
    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }
    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }
    println("Hello,")
    Thread.sleep(2000L)
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
