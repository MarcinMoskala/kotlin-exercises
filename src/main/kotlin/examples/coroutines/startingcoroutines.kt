package examples.startingcoroutines

import kotlinx.coroutines.*

fun main() {
    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }
    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }
    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }
    println("Hello,")
    Thread.sleep(2000L)
}

//suspend fun main() {
//    val job1 = GlobalScope.launch {
//        delay(1000L)
//        println("World!")
//    }
//    val job2 = GlobalScope.launch {
//        delay(1000L)
//        println("World!")
//    }
//    val job3 = GlobalScope.launch {
//        delay(1000L)
//        println("World!")
//    }
//    println("Hello,")
//    job1.join()
//    job2.join()
//    job3.join()
//}

//suspend fun main() {
//    val value1 = GlobalScope.async {
//        delay(2000L)
//        1
//    }
//    val value2 = GlobalScope.async {
//        delay(2000L)
//        2
//    }
//    val value3 = GlobalScope.async {
//        delay(2000L)
//        3
//    }
//    println("Calculating")
//    print(value1.await())
//    print(value2.await())
//    print(value3.await())
//}

//suspend fun main() {
//    val value = GlobalScope.async {
//        delay(2000L)
//        1
//    }
//    println("Calculating")
//    print(value.await())
//    print(value.await())
//    print(value.await())
//}

//fun main() {
//    runBlocking {
//        delay(1000L)
//        println("World!")
//    }
//    runBlocking {
//        delay(1000L)
//        println("World!")
//    }
//    runBlocking {
//        delay(1000L)
//        println("World!")
//    }
//    println("Hello,")
//}

//suspend fun main() {
//    coroutineScope {
//        delay(1000L)
//        println("World!")
//    }
//    coroutineScope {
//        delay(1000L)
//        println("World!")
//    }
//    coroutineScope {
//        delay(1000L)
//        println("World!")
//    }
//    println("Hello,")
//}

//suspend fun main() {
//    println("A")
//    val a: Int = coroutineScope {
//        delay(1000L)
//        10
//    }
//    println("B")
//    val b: Int = coroutineScope {
//        delay(1000L)
//        20
//    }
//    println("C")
//    println(a + b)
//}

//suspend fun main() = coroutineScope {
//    println("A")
//    val a: Deferred<Int> = async {
//        delay(1000L)
//        10
//    }
//    println("B")
//    val b: Deferred<Int> = async {
//        delay(1000L)
//        20
//    }
//    println("C")
//    println(a.await() + b.await())
//}

//suspend fun longTask() = coroutineScope {
//    launch {
//        delay(1000)
//        println("Finished task 1")
//    }
//    launch {
//        delay(2000)
//        println("Finished task 2")
//    }
//}
//
//suspend fun main() {
//    println("Before")
//    longTask()
//    println("After")
//}
