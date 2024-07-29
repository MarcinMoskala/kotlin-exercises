package examples.synclist

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class User(val name: String)

suspend fun main() {
   var users = listOf<User>()
   coroutineScope {
       for (i in 1..1000) {
           launch {
               delay(10)
               users += User("User$i")
           }
       }
   }
   print(users.size)
}
