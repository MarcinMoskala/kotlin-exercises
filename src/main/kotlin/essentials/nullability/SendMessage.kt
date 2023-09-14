package safe

import org.junit.Test
import kotlin.test.assertEquals

class Client(val personalInfo: PersonalInfo?)
class PersonalInfo(val email: String?)

interface Mailer {
    fun sendMessage(email: String, message: String)
}

// Sends a message if message and client's personal info email are not null
fun sendMessageToClient(client: Client?, message: String?, mailer: Mailer) {
    TODO()
}

@Suppress("FunctionName")
internal class NullabilityTests {

    class MailCollector(): Mailer {
        data class Mail(val email: String, val message: String)

        var emails = listOf<Mail>()

        override fun sendMessage(email: String, message: String) {
            emails += Mail(email, message)
        }
    }

    @Test
    fun `When client is null, email we do not send email`() {
        val mailer = MailCollector()
        sendMessageToClient(null, "AAA", mailer)
        assertEquals(emptyList(), mailer.emails)
    }

    @Test
    fun `When message is null, we do not send email`() {
        val mailer = MailCollector()
        sendMessageToClient(Client(PersonalInfo("AAA")), null, mailer)
        assertEquals(emptyList(), mailer.emails)
    }

    @Test
    fun `When personal info is null, we do not send email`() {
        val mailer = MailCollector()
        sendMessageToClient(Client(null), "AAA", mailer)
        assertEquals(emptyList(), mailer.emails)
    }

    @Test
    fun `When email address is null, we do not send email`() {
        val mailer = MailCollector()
        sendMessageToClient(Client(PersonalInfo(null)), null, mailer)
        assertEquals(emptyList(), mailer.emails)
    }

    @Test
    fun `Sends messages correctly for correct values`() {
        val mailer = MailCollector()
        sendMessageToClient(Client(PersonalInfo("AAA")), "BBB", mailer)
        assertEquals(listOf(MailCollector.Mail("AAA", "BBB")), mailer.emails)
    }
}

class NullabilityThrowingTests {

    class MailCollector() : Mailer {
        data class Mail(val email: String, val message: String)

        var emails = listOf<Mail>()

        override fun sendMessage(email: String, message: String) {
            emails += Mail(email, message)
        }
    }

    @Test
    fun `When client is null, email is not sent`() {
        val mailer = MailCollector()
        val res = runCatching { sendMessageToClient(null, "AAA", mailer) }
        val exception = res.exceptionOrNull()
        assert(exception != null) { "Exception not thrown" }
        assert(exception is IllegalArgumentException) { "Exception is $exception, and it should be of type IllegalArgumentException" }
        assertEquals(emptyList<MailCollector.Mail>(), mailer.emails)
    }

    @Test
    fun `When message is null, email is not sent`() {
        val mailer = MailCollector()
        val res = runCatching { sendMessageToClient(Client(PersonalInfo("AAA")), null, mailer) }
        val exception = res.exceptionOrNull()
        assert(exception != null) { "Exception not thrown" }
        assert(exception is IllegalArgumentException) { "Exception is $exception, and it should be of type IllegalArgumentException" }
        assertEquals(emptyList<MailCollector.Mail>(), mailer.emails)
    }

    @Test
    fun `When personal info is null, email is not sent`() {
        val mailer = MailCollector()
        val res = runCatching { sendMessageToClient(Client(null), "AAA", mailer) }
        val exception = res.exceptionOrNull()
        assert(exception != null) { "Exception not thrown" }
        assert(exception is IllegalArgumentException) { "Exception is $exception, and it should be of type IllegalArgumentException" }
        assertEquals(emptyList<MailCollector.Mail>(), mailer.emails)
    }

    @Test
    fun `When email is null, email is not sent`() {
        val mailer = MailCollector()
        val res = runCatching { sendMessageToClient(Client(PersonalInfo(null)), null, mailer) }
        val exception = res.exceptionOrNull()
        assert(exception != null) { "Exception not thrown" }
        assert(exception is IllegalArgumentException) { "Exception is $exception, and it should be of type IllegalArgumentException" }
        assertEquals(emptyList<MailCollector.Mail>(), mailer.emails)
    }

    @Test
    fun `Sends messages correctly`() {
        val mailer = MailCollector()
        sendMessageToClient(Client(PersonalInfo("AAA")), "BBB", mailer)
        assertEquals(listOf(MailCollector.Mail("AAA", "BBB")), mailer.emails)
    }
}
