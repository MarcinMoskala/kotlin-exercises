package backend

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

data class User(val name: String)

fun setupApi(database: Database, emailService: EmailService) {
    api {
        get("hello") {
            "Hello, world"
        }
        get("slow_hello") {
            delay(1000)
            "Hello, world"
        }
        get("users") {
            // TODO: Return users from DB
        }
        post<User>("user") { user ->
            // TODO: Add user to DB and send email to "contact@kt.academy" with body "New user $name"
        }
        get("users/count") {
            // TODO: Get users count
        }
    }.start()
}

fun main() {
    val database = DatabaseImpl()
    val emailService = EmailServiceImpl()
    setupApi(database, emailService)
    runBlocking {
        println(get("hello"))
        println("User count is ${get("user/count")}")
        println("Users are ${get("users")}")
        post("user", User("Marcin"))
    }
}

