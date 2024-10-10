package examples.coroutines

import kotlinx.coroutines.*

//fun main(): Unit = runBlocking {
//    val scope = CoroutineScope(Job())
//    scope.launch {
//        delay(1000)
//        throw Error("Some error")
//    }
//    scope.launch {
//        delay(2000)
//        println("Second coroutine finished")
//    }
//
//    delay(3000)
//    println(scope.isActive)
//}

//fun main(): Unit = runBlocking {
//   coroutineScope {
//       launch {
//           delay(1000)
//           throw Exception("Some error") // First coroutine has an exception
//       }
//       launch {
//           delay(2000)
//           println("Second coroutine finished")
//       }
//       launch {
//           delay(2000)
//           println("Third coroutine finished")
//       }
//   }
//   println("Done")
//}
