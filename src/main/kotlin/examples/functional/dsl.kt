package dsl

data class User(val name: String, val roles: List<String>)

fun main() {
    println(generatePersonelRaport(includeGuests = true))
}

fun generatePersonelRaport(includeGuests: Boolean): String {
    val users = mutableListOf<User>()
    users.add(User("Marcin", listOf("Founder")))
    users.addAll(workers())

    if (includeGuests) {
        users.addAll(guests())
    }

    // There always must be an admin
    users.add(User("Admin", listOf("Admin")))

    val raport = StringBuilder()
    raport.append("User Report:\n")
    users.forEach { user ->
        raport.append("Name: ${user.name}\n")
        raport.append("Roles: ")
        if (user.roles.isEmpty()) {
            raport.append("None")
        } else {
            raport.append(user.roles.joinToString(", "))
        }
        raport.append("\n\n")
    }
    return raport.toString()
}
//fun generatePersonelRaport(includeGuests: Boolean): String {
//    val users = buildList {
//        add(User("Marcin", listOf("Founder")))
//        addAll(workers())
//
//        if (includeGuests) {
//            addAll(guests())
//        }
//
//        // There always must be an admin
//        add(User("Admin", listOf("Admin")))
//    }
//
//    return buildString {
//        append("User Report:\n")
//        users.forEach { user ->
//            append("Name: ${user.name}\n")
//            append("Roles: ")
//            if (user.roles.isEmpty()) {
//                append("None")
//            } else {
//                append(user.roles.joinToString(", "))
//            }
//            append("\n\n")
//        }
//    }
//}

fun workers() = listOf(
    User("Alice", listOf("Admin", "Manager")),
    User("Bob", listOf("User")),
    User("Charlie", listOf("User", "Editor"))
)

fun guests() = listOf(
    User("Mike", listOf("Guest")),
    User("Nancy", listOf("Guest"))
)

// ***

fun sendEmailToBuyer(buyerEmail: String) {
    val email = EmailMessage() 
    email.from = EmailMessage.Email()
    email.from!!.name = "noreply"
    email.from!!.email = "contact@kt.academy"
    email.to = EmailMessage.Email()
    email.to!!.name = "buyer"
    email.to!!.email = buyerEmail
    email.subject = "Your purchase"
    email.body = EmailMessage.Content()
    email.body!!.type = "text/plain"
    email.body!!.value = "Thank you for your purchase!"
    email.send()
}

class EmailMessage {
    var from: Email? = null
    var to: Email? = null
    var bcc: Email? = null
    var cc: Email? = null
    var subject: String? = null
    var body: Content? = null
    
    fun send() {
        println("Sending email to $to from $from")
        println("Subject: $subject")
        println("Body: $body")
    }
    
    class Content {
        var type: String = ""
        var value: String = ""
    }
    
    class Email {
        var name: String = ""
        var email: String = ""
    }
}
