package examples.mutation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

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
//    val repo = UserRepository()
//    val users = repo.loadAll()
//    users[1] = "ABC" // Should not be possible!
//    println(repo.loadAll()) // {1=ABC}
}

class UserRepositoryTest {
    @Test
    fun shouldNotLeakAccessToInternalList() {
        val userRepository = UserRepository()
        val storedUsers = userRepository.loadAll()
        userRepository.add(1, "ABC")
        assertEquals(emptyMap(), storedUsers)
    }

    @Test
    fun shouldAllowConcurrentAccess() = runBlocking(Dispatchers.IO) {
        val userRepository = UserRepository()
        val usersNum = 10_000
        coroutineScope {
            repeat(usersNum) {
                launch { userRepository.add(it, "User$it") }
                if (it % 100 == 0) {
                    launch {
                        val all = userRepository.loadAll()
                        all.forEach { (id, name) -> assertEquals("User$id", name) }
                    }
                }   
            }
        }
        assertEquals(usersNum, userRepository.loadAll().size)
    }
}
