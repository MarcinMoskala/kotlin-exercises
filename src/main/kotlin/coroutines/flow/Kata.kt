package flow

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals

// Produces a flow of Unit
// For instance producingUnits(5) -> [Unit, Unit, Unit, Unit, Unit]
fun producingUnits(num: Int): Flow<Unit> = TODO()

// Adds a delay of time `timeMillis` between elements
fun <T> Flow<T>.delayEach(timeMillis: Long): Flow<T> = TODO()

// Should transform values, where transformation value should have index of the element
// flowOf("A", "B").mapIndexed { index, value -> "$index$value" } -> ["0A", "0B"]
fun <T, R> Flow<T>.mapIndexed(transformation: suspend (index: Int, T) -> R): Flow<R> = TODO()

// Should transform Unit's to toggled boolean value starting from true
// For instance flowOf(Unit, Unit, Unit, Unit).toNextNumbers() -> [true, false, true, false]
// Example:
// Input   --------U------UU---------U------
// Result  --------t------ft---------f------
fun Flow<*>.toToggle(): Flow<Boolean> = TODO()

// Should transform Unit's to next numbers startling from 1
// For instance flowOf(Unit, Unit, Unit, Unit).toNextNumbers() -> [1, 2, 3, 4]
// Example:
// Input   --------U------UU---------U------
// Result  --------1------23---------4------
fun Flow<*>.toNextNumbers(): Flow<Int> = TODO()

// Produces not only elements, but the whole history till now
// For instance flowOf(1, "A", 'C').withHistory() -> [[], [1], [1, A], [1, A, C]]
fun <T> Flow<T>.withHistory(): Flow<List<T>> = TODO()

// Should create a flow that every `tickEveryMillis` should emit next numbers from `startNum` to `endNum`.
// The first number should be emitted immediately, each next one after `tickEveryMillis` delay.
// Should be conflated.
// Example:
// makeTimer(1000, 5, 8).collect { println("$currentTime ms -> $it") }
// 0 ms -> 5
// 1000 ms -> 6
// 2000 ms -> 7
// 3000 ms -> 8
// makeTimer(1000, 5, 9).collect { delay(2500); println("$currentTime ms -> $it") }
// 2500 ms -> 5
// 5000 ms -> 7
// 7500 ms -> 9
fun makeTimer(tickEveryMillis: Long, startNum: Int, endNum: Int): Flow<Int> = TODO()

// Based on two light switches, should decide if the general light should be switched on.
// Should be if one is true and another is false.
// The first state should be false, and then with each even from any switch, new state should be emitted.
// Example:
// switch1 -------t-----f----------t-t-------
// switch2 ----------------f-t-f--------t-f-t
// Result  f------t-----f--f-t-f---t-t--f-t-f
fun makeLightSwitch(switch1: Flow<Boolean>, switch2: Flow<Boolean>): Flow<Boolean> = TODO()

// Based on two light switches, should decide if the general light should be switched on.
// Should be if one is turned on and another is off
// Each event from a switch emits news state. The first state is true, each new state is toggled.
// Example:
// switch1 -------U-----U--------------------------U---------------UU-----
// switch2 ----------------U-------------------U-------------U------------
// Result  -------t-----f--t-------------------f---t---------f-----tf-----
fun makeLightSwitchToggle(switch1: Flow<Unit>, switch2: Flow<Unit>): Flow<Boolean> = TODO()

fun polonaisePairing(track1: Flow<Person>, track2: Flow<Person>): Flow<Pair<Person, Person>> = TODO()

data class Person(val name: String)

@OptIn(ExperimentalCoroutinesApi::class)
private fun <T> Flow<T>.withVirtualTime(testScope: TestScope): Flow<ValueAndTime<T>> =
    map { ValueAndTime(it, testScope.currentTime) }

data class ValueAndTime<T>(val value: T, val timeMillis: Long)

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("FunctionName")
class FlowTests {

    @Test()
    fun producingUnitsTests() = runTest {
        assertEquals(listOf(), producingUnits(0).toList())
        assertEquals(listOf(Unit), producingUnits(1).toList())
        assertEquals(listOf(Unit, Unit), producingUnits(2).toList())
        assertEquals(listOf(Unit, Unit, Unit), producingUnits(3).toList())
        for (i in 1..100 step 7) {
            assertEquals(List(i) { Unit }, producingUnits(i).toList())
        }
    }

    @Test()
    fun flowDelayEachTests() = runTest {
        val emittedNum = AtomicInteger()

        producingUnits(100)
            .delayEach(1000)
            .onEach { emittedNum.incrementAndGet() }
            .launchIn(this)

        assertEquals(0, emittedNum.get())

        // After 1 500ms there should be one element
        delay(1_500)
        assertEquals(1, emittedNum.get())

        // After another 2 000ms there should be two more elements
        delay(2_000)
        assertEquals(3, emittedNum.get())

        // After another 12 000ms there should be twelve more elements
        delay(12_000)
        assertEquals(15, emittedNum.get())
    }

    @Test()
    fun mapIndexedTests() = runTest {
        assertEquals(
            listOf("0 A", "1 B", "2 C", "3 D"),
            ('A'..'D').asFlow()
                .mapIndexed { index, letter -> "$index $letter" }
                .toList()
        )

        val actual: List<ValueAndTime<Pair<Int, Any>>> = flow<Any> {
            delay(10)
            emit(10)
            delay(100)
            emit("A")
            delay(1000)
            emit('C')
        }.mapIndexed { index, any -> Pair(index, any) }
            .withVirtualTime(this)
            .toList()
        val expected: List<ValueAndTime<Pair<Int, Any>>> = listOf(
            ValueAndTime(Pair(0, 10), 10),
            ValueAndTime(Pair(1, "A"), 110),
            ValueAndTime(Pair(2, 'C'), 1110),
        )
        assertEquals(expected, actual)
    }

    @Test()
    fun toToggleTests() = runTest {
        assertEquals(listOf(), producingUnits(0).toToggle().toList())
        assertEquals(listOf(true), producingUnits(1).toToggle().toList())
        assertEquals(listOf(true, false), producingUnits(2).toToggle().toList())
        assertEquals(listOf(true, false, true), producingUnits(3).toToggle().toList())
        assertEquals(listOf(true, false, true, false), producingUnits(4).toToggle().toList())
    }

    @Test()
    fun toNextNumbersTests() = runTest {
        assertEquals(listOf(), producingUnits(0).toNextNumbers().toList())
        assertEquals(listOf(1), producingUnits(1).toNextNumbers().toList())
        assertEquals(listOf(1, 2), producingUnits(2).toNextNumbers().toList())
        assertEquals(listOf(1, 2, 3), producingUnits(3).toNextNumbers().toList())
        for (i in 1..100 step 7) {
            val list = List(i) { it + 1 }
            assertEquals(list, list.map {}.asFlow().toNextNumbers().toList())
        }
    }

    @Test()
    fun withHistoryTests() = runTest {
        assertEquals(listOf(listOf()), producingUnits(0).withHistory().toList())
        assertEquals(listOf(listOf(), listOf(Unit)), producingUnits(1).withHistory().toList())
        assertEquals(listOf(listOf(), listOf(Unit), listOf(Unit, Unit)), producingUnits(2).withHistory().toList())

        assertEquals(
            listOf(listOf(), listOf(1), listOf(1, 2)),
            flowOf(1, 2).withHistory().toList()
        )
        assertEquals(
            listOf(listOf(), listOf(true), listOf(true, false)),
            flowOf(true, false).withHistory().toList()
        )

        val flow = flow {
            delay(10)
            emit("A")
            delay(100)
            emit(10)
            delay(1000)
            emit("C")
        }

        assertEquals(
            listOf(
                ValueAndTime(listOf(), 0),
                ValueAndTime(listOf("A"), 10),
                ValueAndTime(listOf("A", 10), 110),
                ValueAndTime(listOf("A", 10, "C"), 1110),
            ),
            flow.withHistory()
                .withVirtualTime(this)
                .toList()
        )
    }

    @Test()
    fun makeTimerTests() = runTest(UnconfinedTestDispatcher()) {
        val mutex = Mutex()
        var ticked = listOf<Int>()
        makeTimer(1000, 10, 20)
            .onEach {
                mutex.withLock { ticked += it }
            }
            .launchIn(this)

        assertEquals(listOf(10), mutex.withLock { ticked })

        // After 1 500ms there should be one element
        delay(1_500)
        assertEquals(listOf(10, 11), mutex.withLock { ticked })

        // After another 2 000ms there should be two more elements
        delay(2_000)
        assertEquals(listOf(10, 11, 12, 13), mutex.withLock { ticked })

        // After another 12 000ms there should be twelve more elements
        delay(12_000)
        assertEquals((10..20).toList(), mutex.withLock { ticked })
    }

    @Test()
    fun `makeTimer if delayed in between, do not provide old values but only shows the last one`() = runTest {
        val maxValue = 20
        val res = makeTimer(100, 1, maxValue)
            .onEach {
                if (it == 1) delay(50) // To make it clearly after timer delay
                // We don't need to check more often than every 0.5s
                delay(500)
            }
            .toList()

        assertEquals(listOf(1, 6, 11, 16, 20), res)
    }

    @Test()
    fun makeLightSwitchTests() = runTest {
        val switchOne = flow<Boolean> {
            emit(true)
            delay(1000)
            emit(false)
            delay(10)
            emit(true)
            delay(500) // 1500
            emit(false)
        }
        val switchTwo = flow<Boolean> {
            emit(false)
            delay(200)
            emit(true)
            delay(1000) // 1200
            emit(false)
        }

        var lightOn = false
        launch {
            makeLightSwitch(switchOne, switchTwo).collect {
                lightOn = it
            }
        }

        delay(50)
        assertEquals(true, lightOn)
        delay(200) // 250
        assertEquals(false, lightOn)
        delay(800) // 1050
        assertEquals(false, lightOn)
        delay(200) // 1250
        assertEquals(true, lightOn)
        delay(300) // 1550
        assertEquals(false, lightOn)
    }

    // Example:
    // switch1 -------t-----f----------t-t-------
    // switch2 ----------------f-t-f--------t-f-t
    // Result  f------t-----f--f-t-f---t-t--f-t-f
    @Test()
    fun makeLightSwitchExampleTests() = runTest {
        val switch1 = flow {
            delay(7_000)
            emit(true)
            delay(6_000)
            emit(false)
            delay(11_000)
            emit(true)
            delay(2_000)
            emit(true)
        }
        val switch2 = flow {
            delay(16_000)
            emit(false)
            delay(2_000)
            emit(true)
            delay(2_000)
            emit(false)
            delay(9_000)
            emit(true)
            delay(2_000)
            emit(false)
            delay(2_000)
            emit(true)
        }

        val result = makeLightSwitch(switch1, switch2)
            .fold(mapOf<Long, Boolean>()) { acc, e -> acc + (currentTime to e) }

        val expected = mapOf(
            0L to false,
            7_000L to true,
            13_000L to false,
            16_000L to false,
            18_000L to true,
            20_000L to false,
            24_000L to true,
            26_000L to true,
            29_000L to false,
            31_000L to true,
            33_000L to false,
        )
        assertEquals(expected, result)
    }

    @Test()
    fun makeLightSwitchToggleTests() = runTest {
        val switchOne = flow<Unit> {
            emit(Unit)
            delay(1000)
            emit(Unit)
            delay(10)
            emit(Unit)
            delay(500) // 1500
            emit(Unit)
        }
        val switchTwo = flow<Unit> {
            emit(Unit)
            delay(200)
            emit(Unit)
            delay(1000) // 1200
            emit(Unit)
        }

        var lightOn = false
        launch {
            makeLightSwitchToggle(switchOne, switchTwo).collect {
                lightOn = it
            }
        }

        delay(50)
        assertEquals(false, lightOn)
        delay(200) // 250
        assertEquals(true, lightOn)
        delay(800) // 1050
        assertEquals(true, lightOn)
        delay(200) // 1250
        assertEquals(false, lightOn)
        delay(300) // 1550
        assertEquals(true, lightOn)
    }

    // switch1 -------U-----U--------------------------U---------------UU-----
    // switch2 ----------------U-------------------U-------------U------------
    // Result  -------t-----f--t-------------------f---t---------f-----tf-----
    @Test()
    fun makeLightSwitchToggleExampleTests() = runTest {
        val switch1 = flow {
            delay(7_000)
            emit(Unit)
            delay(6_000)
            emit(Unit)
            delay(27_000)
            emit(Unit)
            delay(16_000)
            emit(Unit)
            delay(1_000)
            emit(Unit)
        }
        val switch2 = flow {
            delay(16_000)
            emit(Unit)
            delay(20_000)
            emit(Unit)
            delay(17_000)
            emit(Unit)
        }

        val result = makeLightSwitchToggle(switch1, switch2)
            .fold(mapOf<Long, Boolean>()) { acc, e -> acc + (currentTime to e) }

        val expected = mapOf(
            7_000L to true,
            13_000L to false,
            16_000L to true,
            36_000L to false,
            40_000L to true,
            53_000L to false,
            56_000L to true,
            57_000L to false,
        )
        assertEquals(expected, result)
    }

    @Test()
    fun polonaisePairingTests() = runTest {
        val track1 = flow<Person> {
            emit(Person("A"))
            emit(Person("B"))
            delay(1000)
            emit(Person("C"))
            emit(Person("D"))
        }
        val track2 = flow<Person> {
            emit(Person("1"))
            delay(600)
            emit(Person("2"))
            delay(1000)
            emit(Person("3"))
        }

        val res = polonaisePairing(track1, track2).toList()
        val expected = listOf("A" to "1", "B" to "2", "C" to "3").map { Person(it.first) to Person(it.second) }
        assertEquals(expected, res)

        var lastPair: Pair<Person, Person>? = null
        launch {
            polonaisePairing(track1, track2).collect { lastPair = it }
        }

        runCurrent()
        assertEquals(Person("A") to Person("1"), lastPair)
        delay(200) // 200
        assertEquals(Person("A") to Person("1"), lastPair)

        delay(500) // 700
        assertEquals(Person("B") to Person("2"), lastPair)
        delay(500) // 1200
        assertEquals(Person("B") to Person("2"), lastPair)

        delay(500) // 1700
        assertEquals(Person("C") to Person("3"), lastPair)
    }
}
