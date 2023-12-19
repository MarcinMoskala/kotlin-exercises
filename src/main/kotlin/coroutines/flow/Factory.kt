package flow

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Random
import kotlin.test.assertEquals

// Finish the below implementation using a flow.
//
// Implement a factory using a flow. You should start by creating 5 machines,
// each every 800 ms, and those machines should produce codes every second.
// You should produce 20 codes in total. Each code should be consumed using control.storeCode.

suspend fun setupFactory(control: FactoryControl) {
    // TODO
}

fun main() = runBlocking<Unit> {
    setupFactory(StandardFactoryControl())
}

interface FactoryControl {
    fun makeMachine(): Machine
    fun storeCode(code: String)
}

class StandardFactoryControl : FactoryControl {
    private var broken = false
    private var waiting = false
    private var codes = listOf<String>()

    override fun makeMachine(): Machine = StandardMachine()
        .also { println("Newly created machine") }

    override fun storeCode(code: String) {
        if (waiting || broken) {
            println("Factory control is broken due to 2 attempts to store code at the same time")
            broken = true
            throw BrokenMachineError()
        }
        waiting = true
        Thread.sleep(500)
        waiting = false
        codes = codes + code
        println("Newly stored code is $code")
    }
}

interface Machine {
    fun produce(): String
}

class StandardMachine : Machine {
    private var broken = false

    override fun produce(): String =
        if (broken) throw BrokenMachineError()
        else (1..5).map { letters[random.nextInt(letters.size)] }.joinToString(separator = "")
            .also { println("Newly produced code $it") }

    companion object {
        private val letters = ('a'..'z') + ('0'..'9')
        private val random = Random()
    }
}

class ProductionError : Throwable()
class BrokenMachineError : Throwable()

@ObsoleteCoroutinesApi
@Suppress("FunctionName")
class FactoryTests {

    class FakeFactoryControl : FactoryControl {
        var createdMachines = listOf<PerfectMachine>()
        var codesStored = listOf<String>()

        override fun makeMachine(): Machine {
            return PerfectMachine()
                .also { createdMachines = createdMachines + it }
        }

        override fun storeCode(code: String) {
            codesStored = codesStored + code
        }

        fun countCreatedCodes(): Int = createdMachines.sumBy { it.timesUsed }
    }

    class PerfectMachine : Machine {
        var timesUsed = 0

        override fun produce(): String {
            return (timesUsed++).toString()
        }
    }

    @Test
    fun `Function produces 20 codes in total`() = runTest(UnconfinedTestDispatcher()) {
        val control = FakeFactoryControl()

        setupFactory(control)
        assertEquals(20, control.codesStored.size)
        assertEquals(20, control.countCreatedCodes())
    }


    @Test
    fun `There are 5 machines created in total`() = runTest(UnconfinedTestDispatcher()) {
        val control = FakeFactoryControl()

        setupFactory(control)
        assertEquals(5, control.createdMachines.count())
    }

    /*
     800     1600    1800    2400   2600  2800  3200 3400 3600 3800
      m1 ----------> CODE --------------> CODE --------------> CODE
              m1 -----------------> CODE ---------------> CODE -----
                              m3 ------------------> CODE ----------
                                                 m4 ----------------
Codes                    1              2     3          4    5    6
 */
    @Test
    fun `Machines are produced every 800ms and codes every second`() = runTest(UnconfinedTestDispatcher()) {
        val control = FakeFactoryControl()

        suspend fun checkAfter(timeMillis: Long, codes: Int) {
            delay(timeMillis - currentTime)
            assertEquals(
                codes,
                control.countCreatedCodes(),
                "After $timeMillis (is $currentTime) there should be $codes produced but is ${control.countCreatedCodes()}"
            )
            assertEquals(
                codes,
                control.codesStored.size,
                "After $timeMillis (is $currentTime) there should be $codes stored but is ${control.countCreatedCodes()}"
            )
        }

        launch {
            setupFactory(control)
        }
        checkAfter(800, 0)
        checkAfter(1600, 0)
        checkAfter(1800, 1)
        checkAfter(2400, 1)
        checkAfter(2600, 2)
        checkAfter(2800, 3)
        checkAfter(3200, 3)
        checkAfter(3400, 4)
        checkAfter(3600, 5)
        checkAfter(3800, 6)
    }
}
