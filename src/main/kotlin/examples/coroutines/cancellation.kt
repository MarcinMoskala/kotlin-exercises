package examples.coroutines

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

//suspend fun trySendUntilSuccess() {
//    var success = false
//    do {
//        try {
//            send()
//            success = true
//        } catch (e: Exception) {
//            println("Error while sending: ${e.message}")
//            e.printStackTrace()
//        }
//    } while (!success)
//}
//
//fun main() = runBlocking {
//    val job = launch { trySendUntilSuccess() }
//    delay(100)
//    job.cancelAndJoin()
//}
//
//suspend fun send() {
//    println("Sending...")
//    delay(1000)
//}
