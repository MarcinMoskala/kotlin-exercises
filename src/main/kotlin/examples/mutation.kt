package examples.mutation

data class User(val name: String)

class UserRepository {
   private val storedUsers: MutableMap<Int, String> = mutableMapOf()

   fun loadAll() = storedUsers
  
   fun add(id: Int, name: String) {
       storedUsers[id] = name
   }
   //...
}

fun main() {
    val repository = UserRepository()
    repository.add(1, "Alice")
    repository.add(2, "Bob")
    val users = repository.loadAll()
    println(users)
}
