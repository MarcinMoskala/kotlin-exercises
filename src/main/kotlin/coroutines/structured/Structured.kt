package structured

import kotlinx.coroutines.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.ZonedDateTime
import java.util.Random
import kotlin.test.assertEquals

// We have a worker who makes machines every 800ms as long as there is less than 5 of them.
//   He won't produce more than 1000 machines. Please, use `repeat(1000)` instead of `while(true)`
// Every machine produces a code using `structured.produce` function every second. It saves this code to shared space.
//   In case of an error, it stops working.
//   Machine won't produce more than 1000 codes. Please, use `repeat(1000)` instead of `while(true)`
// We have a single manager that takes codes one after another and stores them using `control.storeCode`.
//   Note that is it time consuming operation.
//   He is the only one who can do that.
//   In case of no codes, he sleeps for 100ms
//   He ends everything when there are 20 codes stored.
//   He won't do it more than 1000 times. Please, use `repeat(1000)` instead of `while(true)`

fun main() = runBlocking<Unit> {
    setupFactory(StandardFactoryControl())
}

fun CoroutineScope.setupFactory(control: FactoryControl) = launch {
    val factory = StructuredFactory()
    launch {
        factory.makeWorker(control)
    }
    factory.makeManager(this, control)
}

class StructuredFactory {
    private val codes = mutableListOf<String>()

    // Make machine using `control.makeMachine()` and then use it to create codes in a separate coroutine every 1000 ms.
    // Codes should be stored in the `codes`. Should first wait, and then produce.
    @Throws(ProductionError::class)
    suspend fun makeMachine(control: FactoryControl): Unit = coroutineScope {
        // TODO
    }

    // Makes machines every 800ms, but there should be no more than 5 active machines at the same time.
    suspend fun makeWorker(control: FactoryControl): Unit = coroutineScope {
        // TODO
    }

    // Checks out the codes and if there is no, waits for 100ms. Otherwise takes the code and stores it using `control.storeCode(code)`.
    // When 20'th code were sent, ends the whole process.
    suspend fun makeManager(scope: CoroutineScope, control: FactoryControl): Unit = coroutineScope {
        // TODO
    }
}

interface FactoryControl {
    fun makeMachine(): Machine
    fun storeCode(code: String)
}

class StandardFactoryControl : FactoryControl {
    private var broken = false
    private var waiting = false
    private var codes = listOf<String>()
    private var lastMachineProducedTimestamp: ZonedDateTime? = null

    override fun makeMachine(): Machine = when {
        lastMachineProducedTimestamp?.let { ZonedDateTime.now() > it.plusNanos(700_000_000) } == false ->
            throw IncorrectUseError("Need to wait 800ms between making machines")
        else -> StandardMachine()
            .also { lastMachineProducedTimestamp = ZonedDateTime.now() }
            .also { println("Newly created machine") }
    }

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
    @Throws(ProductionError::class)
    fun produce(): String
}

class StandardMachine : Machine {
    private var broken = false
    private var lastCodeProducedTimestamp: ZonedDateTime? = null

    override fun produce(): String = when {
        broken ->
            throw BrokenMachineError()
        lastCodeProducedTimestamp?.let { ZonedDateTime.now() > it.plusSeconds(1) } == false ->
            throw IncorrectUseError("Need to wait 1s between uses of the same machine")
        random.nextInt(8) == 0 -> {
            broken = true
            println("Machine broken")
            throw ProductionError()
        }
        else -> (1..5).map { letters[random.nextInt(letters.size)] }.joinToString(separator = "")
            .also { lastCodeProducedTimestamp = ZonedDateTime.now() }
            .also { println("Newly produced code $it") }
    }

    companion object {
        private val letters = ('a'..'z') + ('0'..'9')
        private val random = Random()
    }
}

class ProductionError() : Throwable()
class BrokenMachineError() : Throwable()
class IncorrectUseError(message: String) : Throwable(message)

@ObsoleteCoroutinesApi
@Suppress("FunctionName")
class StructuredTests {

    class FakeFactoryControl(
        val machineProducer: () -> Machine
    ) : FactoryControl {
        var createdMachines = listOf<Machine>()
        var codesStored = listOf<String>()
        private var finished = false

        override fun makeMachine(): Machine {
            require(!finished)
            return machineProducer()
                .also { createdMachines = createdMachines + it }
        }

        override fun storeCode(code: String) {
            require(!finished)
            codesStored = codesStored + code
        }

        fun finish() {
            finished = true
        }
    }

    class PerfectMachine : Machine {
        var timesUsed = 0
        private var finished = false

        override fun produce(): String {
            require(!finished)
            return (timesUsed++).toString()
        }

        fun finish() {
            finished = true
        }
    }

    class FailingMachine : Machine {
        override fun produce(): String = throw ProductionError()
    }

    @Test(timeout = 500)
    fun `PerfectMachine produces next numbers`() {
        val machine = PerfectMachine()
        assertEquals("0", machine.produce())
        assertEquals("1", machine.produce())
        assertEquals("2", machine.produce())
        assertEquals("3", machine.produce())
        assertEquals("4", machine.produce())
    }

    @Test(timeout = 500)
    fun `FakeFactoryControl produces machines using producer`() {
        val perfectFactoryControl = FakeFactoryControl(machineProducer = ::PerfectMachine)
        val machine1 = perfectFactoryControl.makeMachine()
        assertEquals("0", machine1.produce())
        assertEquals("1", machine1.produce())
        assertEquals("2", machine1.produce())
        val machine2 = perfectFactoryControl.makeMachine()
        assertEquals("0", machine2.produce())
        assertEquals("1", machine2.produce())
        assertEquals("2", machine2.produce())

        val failingFactoryControl = FakeFactoryControl(machineProducer = ::FailingMachine)
        val machine3 = failingFactoryControl.makeMachine()
        assertThrows<ProductionError> { machine3.produce() }
    }

    @Test
    fun `Function creates a new machine every 800ms up to 5 and no more if they are all perfect`() = runTest {
        val control = FakeFactoryControl(machineProducer = ::PerfectMachine)
        setupFactory(control)
        for (i in 0..5) {
            assertEquals(i, control.createdMachines.size)
            delay(800)
        }
        for (i in 0..10) {
            assertEquals(5, control.createdMachines.size)
            delay(800)
        }
    }

    @Test
    fun `Function creates a new machine every 800ms every time if all machines are failing`() = runTest {
        val control = FakeFactoryControl(machineProducer = ::FailingMachine)
        setupFactory(control)
        for (i in 0..100) {
            assertEquals(i, control.createdMachines.size)
            delay(800)
        }
    }

    @Test
    fun `Function creates a new machine after 800ms if less then 5`() = runTest {
        var correctMachines = 0
        var nextIsCorrect = false
        val control = FakeFactoryControl(machineProducer = {
            val next = if (nextIsCorrect) {
                correctMachines++
                PerfectMachine()
            } else {
                FailingMachine()
            }
            nextIsCorrect = !nextIsCorrect
            next
        })

        setupFactory(control)
        delay(20_000)

        assertEquals(5, control.createdMachines.filterIsInstance<PerfectMachine>().size)

        // Is not producing any new
        val producedPre = control.createdMachines.size
        delay(2_000)

        val producedPost = control.createdMachines.size
        assertEquals(
            producedPre,
            producedPost,
            "It should not produce any new machines when there are already 5 perfect"
        )
    }

    @Test
    fun `The first code should be created after time to create machine and time to produce code (+10ms margin)`() =
        runTest {
            val perfectMachine = PerfectMachine()
            val control = FakeFactoryControl(machineProducer = { perfectMachine })

            setupFactory(control)
            delay(800 + 1000 + 10)

            assertEquals(1, perfectMachine.timesUsed)
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
    fun `Every machine produces code every second`() = runTest {
        val perfectMachine = PerfectMachine()
        val control = FakeFactoryControl(machineProducer = { perfectMachine })
        suspend fun checkAt(timeMillis: Long, codes: Int) {
            delay(timeMillis - currentTime)
            assertEquals(codes, perfectMachine.timesUsed)
        }

        setupFactory(control)
        checkAt(800, 0)
        checkAt(1600, 0)
        checkAt(1800, 1)
        checkAt(2400, 1)
        checkAt(2600, 2)
        checkAt(2800, 3)
        checkAt(3200, 3)
        checkAt(3400, 4)
        checkAt(3600, 5)
        checkAt(3800, 6)
    }

    @Test
    fun `Created codes are stored no later then 100ms after created`() = runTest {
        val perfectMachine = PerfectMachine()
        val control = FakeFactoryControl(machineProducer = { perfectMachine })
        suspend fun checkAt(timeMillis: Long, codes: Int) {
            delay(timeMillis - currentTime)

            assertEquals(codes, control.codesStored.size)
        }
        setupFactory(control)
        checkAt(900, 0)
        checkAt(1700, 0)
        checkAt(1900, 1)
        checkAt(2500, 1)
        checkAt(2700, 2)
        checkAt(2900, 3)
        checkAt(3300, 3)
        checkAt(3500, 4)
        checkAt(3700, 5)
        checkAt(3900, 6)
    }

    @Test
    fun `When there are 20 codes stored, process ends`() = runTest {
        val perfectMachine = PerfectMachine()
        val control = FakeFactoryControl(machineProducer = { perfectMachine })
        setupFactory(control)
        delay(6810) // Time when 20'th code is produced

        assertEquals(20, control.codesStored.size)
        perfectMachine.finish() // To not let it be used anymore
        control.finish() // To not let it be used anymore
        delay(1_000)

        assertEquals(20, control.codesStored.size)
    }

    private inline fun <reified T> assertThrows(body: () -> Unit) {
        val error = try {
            body()
            Any()
        } catch (t: Throwable) {
            t
        }
        assertEquals(T::class, error::class)
    }
}
