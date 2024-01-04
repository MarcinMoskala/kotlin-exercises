package functional.context.petstore

import org.junit.After
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class PetStore(
    private val database: Database,
) {
    fun addPet(
        addPetRequest: AddPetRequest,
    ): Pet? {
        return try {
            database.addPet(addPetRequest)
        } catch (e: InsertionConflictException) {
            null
        } catch (e: Exception) {
            null
        }
    }
}

data class AddPetRequest(val name: String)
data class Pet(val id: Int, val name: String)
class InsertionConflictException : Exception()

interface Database {
    fun addPet(addPetRequest: AddPetRequest): Pet
}

interface Logger {
    fun logInfo(message: String)
    fun logWarning(message: String)
    fun logError(message: String)
}

fun main(): Unit = with(ConsoleLogger()) {
    val database = RandomDatabase()
    val petStore = PetStore(database)
    petStore.addPet(AddPetRequest("Fluffy"))
    // [INFO] - Adding pet with name Fluffy
    // [INFO] - Added pet with id -81731626
    // or
    // [WARNING] - There already is pet named Fluffy
    // or
    // [ERROR] - Failed to add pet with name Fluffy
}

class RandomDatabase : Database {
    override fun addPet(addPetRequest: AddPetRequest): Pet =
        when {
            Random.nextBoolean() -> 
                Pet(1234, addPetRequest.name)
            Random.nextBoolean() -> 
                throw InsertionConflictException()
            else -> throw Exception()
        }
}

class ConsoleLogger : Logger {
    override fun logInfo(message: String) {
        println("[INFO] - $message")
    }

    override fun logWarning(message: String) {
        println("[WARNING] - $message")
    }

    override fun logError(message: String) {
        println("[ERROR] - $message")
    }
}

class PetStoreTest {
    private val database = FakeDatabase()
    private val petStore = PetStore(database)
    private val logger = FakeLogger()

    @After
    fun tearDown() {
        database.clear()
        logger.clear()
    }

    @Test
    fun `should add pet`() {
        val pet = with(logger) {
            petStore.addPet(AddPetRequest("Fluffy"))
        }
        val expected = Pet(0, "Fluffy")
        assertEquals(expected, pet)
        assertEquals(expected, database.getPets().single())
    }

    @Test
    fun `should return null when database failing`() {
        database.startFailing()
        val pet = with(logger) {
            petStore.addPet(AddPetRequest("Fluffy"))
        }
        assertEquals(null, pet)
        assertEquals(emptyList<Pet>(), database.getPets())
    }

    @Test
    fun `should return null when conflict`() {
        database.addPet(AddPetRequest("Fluffy"))
        val pet = with(logger) {
            petStore.addPet(AddPetRequest("Fluffy"))
        }
        assertEquals(null, pet)
    }

    @Test
    fun `should log info when added pet`() {
        with(logger) {
            petStore.addPet(AddPetRequest("Fluffy"))
        }
        assertEquals(
            listOf(
                FakeLogger.Level.INFO to "Adding pet with name Fluffy",
                FakeLogger.Level.INFO to "Added pet with id 0",
            ),
            logger.getMessages()
        )
    }

    @Test
    fun `should log warning when adding conflict`() {
        database.addPet(AddPetRequest("Fluffy"))
        with(logger) {
            petStore.addPet(AddPetRequest("Fluffy"))
        }
        assertEquals(
            listOf(
                FakeLogger.Level.INFO to "Adding pet with name Fluffy",
                FakeLogger.Level.WARNING to "There already is pet named Fluffy",
            ),
            logger.getMessages()
        )
    }

    @Test
    fun `should log error when database error`() {
        database.startFailing()
        with(logger) {
            petStore.addPet(AddPetRequest("Fluffy"))
        }
        assertEquals(
            listOf(
                FakeLogger.Level.INFO to "Adding pet with name Fluffy",
                FakeLogger.Level.ERROR to "Failed to add pet with name Fluffy",
            ),
            logger.getMessages()
        )
    }
}

class FakeLogger : Logger {
    private val messages = mutableListOf<Pair<Level, String>>()

    fun clear() {
        messages.clear()
    }

    fun getMessages(): List<Pair<Level, String>> = messages.toList()

    override fun logInfo(message: String) {
        messages.add(Level.INFO to message)
    }

    override fun logWarning(message: String) {
        messages.add(Level.WARNING to message)
    }

    override fun logError(message: String) {
        messages.add(Level.ERROR to message)
    }

    enum class Level {
        INFO, WARNING, ERROR
    }
}

class FakeDatabase : Database {
    private val pets = mutableListOf<Pet>()
    private var shouldFail = false

    fun startFailing() {
        shouldFail = true
    }

    fun clear() {
        pets.clear()
        shouldFail = false
    }

    override fun addPet(addPetRequest: AddPetRequest): Pet {
        if (pets.any { it.name == addPetRequest.name }) throw InsertionConflictException()
        if (shouldFail) throw Exception()
        val pet = Pet(pets.size, addPetRequest.name)
        pets.add(pet)
        return pet
    }

    fun getPets(): List<Pet> = pets.toList()
}
