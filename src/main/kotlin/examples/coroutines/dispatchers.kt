package examples.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.setMain
import kotlin.random.Random


suspend fun main() = coroutineScope {
    val randomList = List(1_000_000) { Random.nextInt() }
    repeat(1000) {
        launch(Dispatchers.Default) {
            // To make it busy
            randomList.sorted()

            val threadName = Thread.currentThread().name
            println("Running on thread: $threadName")
        }
    }
}

//suspend fun main() = coroutineScope {
//    Dispatchers.setMain(newSingleThreadContext("main"))
//    
//    repeat(1000) {
//        launch(Dispatchers.Main) {
//            // To make it busy
//            List(1000) { Random.nextLong() }.maxOrNull()
//
//            val threadName = Thread.currentThread().name
//            println("Running on thread: $threadName")
//        }
//    }
//}

//suspend fun main() = coroutineScope {
//    repeat(1000) {
//        launch(Dispatchers.IO) {
//            // To make it busy
//            Thread.sleep(1000)
//
//            val threadName = Thread.currentThread().name
//            println("Running on thread: $threadName")
//        }
//    }
//}
