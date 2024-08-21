package examples.essentials.data

class UserRepository {
    private val users = mutableMapOf<Int, User>()
    
    fun getUser(id: Int): User? = users[id]
    
    fun addUser(user: User) {
        users[user.id] = user
    }
}

data class User(val id: Int, val name: String, val age: Int)

fun main() {
//    val userRepository = UserRepository()
//    println(userRepository) 
//    // examples.essentials.data.UserRepository@87aac27
//    
//    val user = User(1, "Alice", 25)
//    println(user) 
//    // User(id=1, name=Alice, age=25)
//    
//    println(user)
//    println(user.toString())
//    println("$user")
    
//    val userRepository1 = UserRepository()
//    val userRepository2 = UserRepository()
//    println(userRepository1 == userRepository2) // false
//    println(userRepository1 == userRepository1) // true
//    
//    val user1 = User(1, "Alice", 25)
//    val user2 = User(2, "Bob", 30)
//    val user3 = User(1, "Alice", 25)
//    
//    println(user1 == user2) // false
//    println(user1 == user3) // true
    
//    fun birthday(user: User): User {
//        return user.copy(age = user.age + 1)
//    }
//    
//    val user = User(1, "Alice", 25)
//    val olderUser = birthday(user)
//    println(user) // User(id=1, name=Alice, age=25)
//    println(olderUser) // User(id=1, name=Alice, age=26)
    
//    fun copy(
//        id: Int = this.id,
//        name: String = this.name,
//        age: Int = this.age
//    ) = User(id, name, age)
    
//    val pair = Pair(1, "Alice")
//    val (id, name) = pair
//    println("id: $id, name: $name") // id: 1, name: Alice
    
//    val user = User(1, "Alice", 25)
//    val (id, name, age) = user
//    println("id: $id, name: $name, age: $age") // id: 1, name: Alice, age: 25
    
//    val (id, name, age) = user
//    // under the hood
//     val id = user.component1()
//     val name = user.component2()
//     val age = user.component3()
    
//    val user = User(1, "Alice", 25)
//    val (userId, userName, userAge) = user
//    println(userAge) // 25
    
//    val user = User(1, "Alice", 25)
//    val (userAge, userName, userId) = user
//    println(userAge) // 1
}
