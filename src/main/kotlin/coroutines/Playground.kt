import flow.makeTimer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest


fun main(): Unit = runTest {
    makeTimer(1000, 5, 8).collect { println("$currentTime ms -> $it") }
}
