package flow

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

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