package creation

import creation.RequirementsChecks.FakeNotifier.Companion.INCORRECT_ID
import org.junit.Assert.assertThrows
import org.junit.Test
import java.lang.AssertionError
import java.lang.IllegalStateException
import kotlin.test.assertEquals

data class User(val id: Int, val name: String?, var surname: String?)

fun Notifier.notifyUser(user: User?) {
    TODO()
}

class IncorrectId : Error()

interface Notifier {

    /**
     * Indicate instance readiness to notify users
     */
    val initialized: Boolean

    /**
     * Notifies person
     * @param id Is an id of user we want to notify
     * @return Was operation successful
     */
    fun notifyPerson(id: Int): Boolean

    /**
     * Checks if we can send message to the following `id`
     */
    fun checkId(id: Int): Boolean
}

class RequirementsChecks {
    @Test
    fun `Function sends message for correct arguments and state`() {
        val notifier = FakeNotifier(initialized = true)
        val id = 123
        notifier.notifyUser(User(id, "Ben", "Barack"))
        assertEquals(setOf(id), notifier.usersNotified)
    }

    @Test
    fun `Function does nothing when user is null`() {
        val notifier = FakeNotifier(initialized = true)
        notifier.notifyUser(null)
        assertEquals(setOf(), notifier.usersNotified)
    }

    @Test
    fun `Function throws IllegalArgumentException when name is null`() {
        val notifier = FakeNotifier(initialized = true)
        val id = 123
        assertThrows(IllegalArgumentException::class.java) { notifier.notifyUser(User(id, null, "Kowalski")) }
    }

    @Test
    fun `Function throws IllegalArgumentException when surname is null`() {
        val notifier = FakeNotifier(initialized = true)
        val id = 123
        assertThrows(IllegalArgumentException::class.java) { notifier.notifyUser(User(id, "Barack", null)) }
    }

    @Test
    fun `Function throws IllegalStateException when notifier is not initialized`() {
        val notifier = FakeNotifier(initialized = false)
        val id = 123
        assertThrows(IllegalStateException::class.java) { notifier.notifyUser(User(id, "Mike", "Bull")) }
    }

    @Test
    fun `Function throws IncorrectId when id is incorrect`() {
        val notifier = FakeNotifier(initialized = true)
        assertThrows(IncorrectId::class.java) { notifier.notifyUser(User(INCORRECT_ID, "Mike", "Bull")) }
    }

    @Test
    fun `It is expected that notifyPerson will return true to indicate success`() {
        val notifier = FakeNotifier(initialized = true)
        assertThrows(IncorrectId::class.java) { notifier.notifyUser(User(INCORRECT_ID, "Mike", "Bull")) }
    }

    @Test
    fun `Function should check if notifyPerson returns true as expected in correct conditions`() {
        val notifier = FakeNotifier(initialized = true)
        val id = 123
        notifier.incorrectImplementationMakingNotifyPersonReturnFalse = true
        assertThrows(AssertionError::class.java) { notifier.notifyUser(User(id, "Ben", "Barack")) }
    }

    class FakeNotifier(initialized: Boolean = false): Notifier {
        override var initialized = initialized
            private set
        var usersNotified = setOf<Int>()
        var incorrectImplementationMakingNotifyPersonReturnFalse = false

        override fun checkId(id: Int): Boolean = id != INCORRECT_ID

        override fun notifyPerson(id: Int): Boolean {
            if(incorrectImplementationMakingNotifyPersonReturnFalse) return false
            usersNotified = usersNotified + id
            return true
        }
        
        companion object {
            const val INCORRECT_ID = -100
        }
    }
}
