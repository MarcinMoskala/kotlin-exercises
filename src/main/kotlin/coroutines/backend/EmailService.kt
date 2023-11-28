package backend

import kotlinx.coroutines.delay

interface EmailService {
    suspend fun sendEmail(to: String, body: String)
}

class EmailServiceImpl : EmailService {

    override suspend fun sendEmail(to: String, body: String) {
        delay(2000)
        print("Sent email to $to with body $body")
    }
}