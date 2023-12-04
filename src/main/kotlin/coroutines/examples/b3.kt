import kotlinx.coroutines.*
import kotlin.concurrent.thread

fun main() {
    val value1 = GlobalScope.async {
        delay(2000L)
        1
    }
    val value2 = GlobalScope.async {
        delay(2000L)
        2
    }
    val value3 = GlobalScope.async {
        delay(2000L)
        3
    }
    println("Calculating")
    runBlocking {
        print(value1.await())
        print(value2.await())
        print(value3.await())
    }
}
