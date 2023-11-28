package flow

import kotlinx.coroutines.runBlocking
import java.util.Random

// Finish the below implementation using a flow.
//
// Implement a factory using a flow. You should start by creating 5 machines,
// each every 800 ms, and those machines should produce codes every second.
// You should produce 20 codes in total. Each code should be consumed using control.storeCode.

fun main() = runBlocking<Unit> {
    setupFactory(StandardFactoryControl())
}

suspend fun setupFactory(control: FactoryControl) {
    // TODO
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
