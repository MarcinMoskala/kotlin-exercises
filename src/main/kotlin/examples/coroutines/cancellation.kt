package examples.coroutines

import kotlinx.coroutines.*

suspend fun main(): Unit = coroutineScope {
   val job = launch {
       repeat(1_000) { i ->
           delay(200)
           println("Printing $i")
       }
   }

   delay(1100)
   job.cancel()
   job.join()
   println("Cancelled successfully")
}

//suspend fun fetchUser(): User {
//   // Runs forever
//   while (true) {
//       yield()
//   }
//}
//
//suspend fun getUserOrNull(): User? = withTimeoutOrNull(5000) {
//   fetchUser()
//}
//
//suspend fun main(): Unit = coroutineScope {
//   val user = getUserOrNull()
//   println("User: $user")
//}
//
//data class User(val name: String)
