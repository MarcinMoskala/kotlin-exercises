package examples.essentials.extensions

class User(
    val name: String,
    val surname: String,
) 

val User.fullName: String
    get() = "$name $surname"

fun User.printUser() {
    println("$name $surname")
}

fun main() {
    val user = User("John", "Doe")
    println(user.fullName) // John Doe
    user.printUser() // John Doe
    
//    val iterable: Iterable<Int> = (1..100)
//    iterable.toList()
//        .filter { it % 2 == 0 }
//        .map { "Name$it" }
//        .onEach { println(it) }
}
