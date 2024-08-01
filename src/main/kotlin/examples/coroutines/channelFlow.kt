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
    private val users = List(20) { User("User$it") }
    private val pageSize: Int = 3

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
        emitAll(users.asFlow())
    } while (users.isNotEmpty())
}

//suspend fun main() {
//    val api = FakeUserApi()
//    val users = allUsersFlow(api)
//    val user = users
//        .first {
//            println("Checking $it")
//            delay(1000) // suspending
//            it.name == "User3"
//        }
//    println(user)
//}

//suspend fun main() = coroutineScope {
//  flowOf("A", "B", "C")
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

//suspend fun main() = coroutineScope {
//   val flow = flow {
//       for (i in 1..30) {
//           delay(100)
//           emit(i)
//       }
//   }
//
//   print(flow.onEach { delay(1000) }.toList()) 
//   // [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30]
//
//	print(flow.conflate().onEach { delay(1000) }.toList())
//   // [1, 10, 20, 30]
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
