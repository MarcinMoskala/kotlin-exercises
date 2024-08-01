package examples.coroutines

import kotlinx.coroutines.*

//fun main(): Unit = runBlocking {
//    val scope = CoroutineScope(SupervisorJob())
//    scope.launch {
//        delay(1000)
//        throw Error("Some error")
//    }
//    scope.launch {
//        delay(2000)
//        println("Will be printed")
//    }
//
//    delay(3000)
//    println(scope.isActive)
//}

//fun main(): Unit = runBlocking {
//   coroutineScope {
//       launch {
//           delay(1000)
//           throw Error("Some error")
//       }
//       launch {
//           delay(2000)
//           println("Will be printed")
//       }
//       launch {
//           delay(2000)
//           println("Will be printed")
//       }
//   }
//   println("Done")
//}
