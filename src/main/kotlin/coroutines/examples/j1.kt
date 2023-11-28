package examples

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun main() = coroutineScope {
    val job = Job()
    println(job) // JobImpl{Active}@ADD
    job.complete()
    println(job) // JobImpl{Completed}@ADD

    val activeJob = launch {
        // no-op
    }
    println(activeJob) // StandaloneCoroutine{Active}@ADD
    activeJob.join()
    println(activeJob) // StandaloneCoroutine{Completed}@ADD

    val lazyJob = launch(start = CoroutineStart.LAZY) {
        // no-op
    }
    println(lazyJob) // LazyStandaloneCoroutine{New}@ADD
    lazyJob.start()
    println(lazyJob) // LazyStandaloneCoroutine{Active}@ADD
    lazyJob.join()
    println(lazyJob) //LazyStandaloneCoroutine{Completed}@ADD
}