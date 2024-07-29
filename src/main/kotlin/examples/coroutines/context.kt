package examples.coroutines

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

fun main() {
    val ctx1: CoroutineContext = CoroutineName("A")
    val ctx2: CoroutineContext = Job()
    val ctx3: CoroutineContext = ctx1 + ctx2
    
    val nameContext: CoroutineName? = ctx3[CoroutineName]
    println(nameContext?.name)
    
//    println(ctx1[CoroutineName])
//    println(ctx1[Job])
//    println(ctx2[CoroutineName])
//    println(ctx2[Job])
//    println(ctx3[CoroutineName])
//    println(ctx3[Job])
}



//fun CoroutineScope.log(msg: String) {
//   val name = coroutineContext[CoroutineName]?.name
//   println("[$name] $msg")
//}
//
//fun main() = runBlocking(CoroutineName("main")) {
//   log("Started")
//   val v1 = async {
//       delay(500)
//       log("Running async")
//       42
//   }
//   launch {
//       delay(1000)
//       log("Running launch")
//   }
//   log("The answer is ${v1.await()}")
//}
