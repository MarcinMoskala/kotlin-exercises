@file:OptIn(ExperimentalCoroutinesApi::class)

package examples.t3

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals

fun main() = runTest {
    assertEquals(0, currentTime)
    coroutineScope {
        launch { delay(1000) }
        launch { delay(1500) }
        launch { delay(2000) }
    }
    assertEquals(2000, currentTime)
}
