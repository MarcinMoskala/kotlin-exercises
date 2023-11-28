package actors

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import java.util.*

// Finish the below implementation by sending messages and implementing the following actors:
// Worker is informed every 800. If there are less than 5 machines it produces a new one.
// Every machine produces a code using `structured.produce` function every second or breaks (random).
// Manager collects all the codes, and stores them using `storeCode`, and if there are more than 20 stored is ends everything.

fun main() = runBlocking<Unit> {
    setupFactory(StandardFactoryControl())
}

fun CoroutineScope.setupFactory(control: FactoryControl) = launch {
    val managerChannel = managerActor(control)
    val workerChannel = workerActor(control, managerChannel)
    launch {
        repeat(1000) {
            delay(800)
            // TODO
        }
    }
}

fun CoroutineScope.managerActor(control: FactoryControl): SendChannel<ManagerMessages> = TODO()

fun CoroutineScope.workerActor(
    control: FactoryControl,
    managerChannel: SendChannel<ManagerMessages>
): SendChannel<WorkerMessages> = TODO()

fun CoroutineScope.startMachine(
    control: FactoryControl,
    workerChannel: SendChannel<WorkerMessages>,
    managerChannel: SendChannel<ManagerMessages>
) {
    val machine = control.makeMachine()
    launch {
        try {
            repeat(1000) {
                delay(1000)
                val code = machine.produce()
                // TODO
            }
        } catch (error: ProductionError) {
            // TODO
        }
    }
}

interface ManagerMessages
interface WorkerMessages

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
    @Throws(ProductionError::class)
    fun produce(): String
}

class StandardMachine : Machine {
    private var broken = false

    override fun produce(): String = when {
        broken -> throw BrokenMachineError()
        random.nextInt(8) == 0 -> {
            broken = true
            println("Machine broken")
            throw ProductionError()
        }
        else -> (1..5).map { letters[random.nextInt(letters.size)] }.joinToString(separator = "")
            .also { println("Newly produced code $it") }
    }

    companion object {
        private val letters = ('a'..'z') + ('0'..'9')
        private val random = Random()
    }
}

class ProductionError() : Throwable()
class BrokenMachineError() : Throwable()
