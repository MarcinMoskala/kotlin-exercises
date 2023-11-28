package examples

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

fun main() = runBlocking {
    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }
    println("Hello,")
    delay(2000L)
}

class MyTest {
    @Test
    fun testMySuspendingFunction() = runBlocking {
        var a = "AA"
        delay(1000)
        assert(1 == 1)
    }
}
