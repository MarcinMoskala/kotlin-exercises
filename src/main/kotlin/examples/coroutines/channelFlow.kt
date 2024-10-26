package examples.coroutines.channelFlow

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.system.measureTimeMillis

data class User(val name: String)

interface UserApi {
    suspend fun takePage(pageNumber: Int): List<User>
}

class FakeUserApi : UserApi {
    private val users = List(2000) { User("User$it") }
    private val pageSize: Int = 4

    override suspend fun takePage(
        pageNumber: Int
    ): List<User> {
        delay(1000) // suspending
        return users
            .drop(pageSize * pageNumber)
            .take(pageSize)
    }
}

private fun allUsersFlow(api: UserApi): Flow<User> = flow {
    var page = 0
    do {
        println("Fetching page $page")
        val users = api.takePage(page++) // suspending
        for (user in users) {
            emit(user)
        }
    } while (users.isNotEmpty())
}

suspend fun main() {
    val api = FakeUserApi()
    val users = allUsersFlow(api)
    val user = users
        .first {
            println("Checking $it")
            delay(1000) // suspending
            it.name == "User9"
        }
    println(user)
}

//suspend fun main() = coroutineScope {
//  flowOf("A", "B", "C", "D", "E", "F")
//      .onEach  {
//          delay(1000)
//          println("onEach $it")
//      }
//      .buffer(100)
//      .collect {
//          delay(1000)
//          println("collect $it")
//      }
//}

//suspend fun main(): Unit = coroutineScope {
//    flow {
//        for (i in 1..300) {
//            delay(100)
//            println("Emitting $i")
//            emit(i)
//        }
//    }.conflate()
//        .collect {
//            delay(1000)
//            println(it)
//        }
//}

//suspend fun main() {
//    measureTimeMillis {
//        ('A'..'C').asFlow()
//            .flatMapConcat { flowFrom(it) }
//            .collect { print(it) }
//    }.let { println("Took $it ms") }
//}
//
//fun flowFrom(elem: Any) = flow {
//    repeat(3) {
//        delay(1000)
//        emit("${elem}_$it ")
//    }
//}

//suspend fun main() {
//    val f1 = flowOf(1, 2, 3, 4, 5).onEach { delay(1000) }
//    val f2 = flowOf("A", "B", "C").onEach { delay(800) }
//
//    f1.combine(f2) { t1, t2 -> t2 + t1 }
//        .collect { print("$it ") }
//    
//    f1.zip(f2) { t1, t2 -> t2 + t1 }
//        .collect { print("$it ") }
//}
