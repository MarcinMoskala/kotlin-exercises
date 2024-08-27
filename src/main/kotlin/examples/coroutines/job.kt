package examples.coroutines

import kotlinx.coroutines.*

fun main(): Unit = runBlocking {
    // Each coroutine has its own Job instance
    val parentJob = coroutineContext[Job]
    println(parentJob?.isActive) // true

    // When we launch a coroutine, it returns a Job instance
    val job: Job = launch {
        delay(1000)
        println("Test")
    }

    // That is a different instance than its parent
    println(job == coroutineContext[Job]) // false

    // But this instance is a parent of the coroutine
    println(job.parent == parentJob) // true
    println(job == parentJob?.children?.firstOrNull()) // true

    // Deferred is also a Job
    val deferred: Deferred<String> = async {
        delay(1000)
        "Test"
    }
    val job2: Job = deferred

    // Job cannot be inherited! Job passed as an argument becomes a parent
    val job3 = Job()
    val job4 = launch(job3) {
        delay(1000)
        println("Test")
    }
    println(job4 == job3) // false
    println(job4.parent == job3) // true

    // But when a job is passed as an argument, there is no relationship between this coroutine and the scope
    println(job4.parent == parentJob) // false
}

//fun main(): Unit = runBlocking {
//    launch(Job()) { // the new job replaces one from parent
//        delay(1000)
//        println("Will not be printed")
//    }
//}

//suspend fun main() = coroutineScope {
//    val job = Job()
//    println(job)
//    job.complete()
//    println(job)
//
////    val activeJob = launch {
////        launch { delay(2000) }
////        delay(1000)
////    }
////    println(activeJob)
////    delay(1500)
////    println(activeJob)
////    activeJob.join()
////    println(activeJob)
////
////    val lazyJob = launch(start = CoroutineStart.LAZY) {
////        // no-op
////    }
////    println(lazyJob)
////    lazyJob.start()
////    println(lazyJob)
////    lazyJob.join()
////    println(lazyJob)
//}

//fun main(): Unit = runBlocking {
//   val job1 = launch {
//       delay(1000)
//       println("Test1")
//   }
//   val job2 = launch {
//       delay(2000)
//       println("Test2")
//   }
//
//   job1.join()
//   job2.join()
//   println("All tests are done")
//}

//fun main(): Unit = runBlocking {
//    launch {
//        delay(1000)
//        println("Test1")
//    }
//    launch {
//        delay(2000)
//        println("Test2")
//    }
//    coroutineContext[Job]?.children?.forEach { it.join() }
//    println("All tests are done")
//}

//suspend fun main(): Unit = coroutineScope {
//    val job = Job()
//    launch(job) {
//        delay(1000)
//        println("Text 1")
//    }
//    launch(job) {
//        delay(2000)
//        println("Text 2")
//    }
//    job.join()
//    println("After")
//}

//fun main(): Unit = runBlocking {
//   val deferred = CompletableDeferred<String>()
//   launch {
//       println("Starting first")
//       delay(1000)
//       deferred.complete("Test")
//       delay(1000)
//       println("First done")
//   }
//   launch {
//       println("Starting second")
//       println(deferred.await())
//       println("Second done")
//   }
//}
