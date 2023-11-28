package structured

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.ZonedDateTime
import java.util.Random

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
