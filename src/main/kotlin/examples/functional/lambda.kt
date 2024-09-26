package examples.functional.lambda

import java.time.LocalDateTime
import kotlin.concurrent.thread

//fun main() {
//    {
//        println("AAA")
//    }
//}











//fun produce() = { 42 }
//
//fun main() {
//    println(produce()) // ???
//}












// Parameters
//fun main() {
//    val printTimes = { text: String, times: Int ->
//        for (i in 1..times) {
//            print(text)
//        }
//    } // the type is (text: String, times: Int) -> Unit
//    printTimes("Na", 7) // NaNaNaNaNaNaNa
//    printTimes.invoke("Batman", 2) // BatmanBatman
//}

// Trailing lambdas
//inline fun <R> run(block: () -> R): R = block()
//
//inline fun repeat(times: Int, block: (Int) -> Unit) {
//    for (i in 0 until times) {
//        block(i)
//    }
//}
//
//fun main() {
//    run({ println("A") }) // A
//    run() { println("A") } // A
//    run { println("A") } // A
//
//    repeat(2, { print("B") }) // BB
//    println()
//    repeat(2) { print("B") } // BB
//    
//    thread { println("A") }
//}

//fun sum(a: Int, b: Int) = (a..b).fold(0) { acc, i -> acc + i }
//
//fun product(a: Int, b: Int) = (a..b).fold(1) { acc, i -> acc * i }

// Last lambda in argument convention

// Puzzler
//fun call(before: () -> Unit = {}, after: () -> Unit = {}) {
//    before()
//    print("A")
//    after()
//}
//
//fun main() {
//    call({ print("C") })
//    call { print("B") }
//}


// Result values

//fun main() {
//    val f = {
//        10
//        20
//        30
//    }
//    println(f())
//}

//fun main() {
//    onUserChanged { user ->
//        if (user == null) return // compilation error
//        cheerUser(user)
//    }
//}
//
//fun onUserChanged(action: (User?) -> User?) {
//    action(User("Alice"))
//}
//data class User(val name: String)
//fun cheerUser(user: User): User {
//    println("Hello, ${user.name}!")
//    return user
//}

//fun main() {
//    listOf(1, 2, 3, 4, 5).forEach { i -> 
//        if (i == 3) return
//        println(i)
//    }
//}

//fun main() {
//    val magicSquare = listOf(
//        listOf(2, 7, 6),
//        listOf(9, 5, 1),
//        listOf(4, 3, 8),
//    )
//    magicSquare.forEach line@ { line ->
//        var sum = 0
//        line.forEach { elem ->
//            sum += elem
//            if (sum == 15) {
//                return@line
//            }
//        }
//        print("Line $line not correct")
//    }
//}


// An implicit name for a single parameter

//fun produceNewsAdapters(news: List<NewsItem>): List<NewsItemAdapter> =
//    news.filter { it.visible }
//        .sortedByDescending { it.publishedAt }
//        .map { it.toNewsItemAdapter() }
//
//data class Name(val value: String)
//data class NewsItem(val visible: Boolean, val publishedAt: Long, val title: String)
//data class NewsItemAdapter(val title: String, val date: String)
//
//fun NewsItem.toNewsItemAdapter() = NewsItemAdapter(
//    title = title,
//    date = LocalDateTime.ofEpochSecond(publishedAt, 0, null).toString()
//)

// Closures

//fun makeCounter(): () -> Int {
//    var i = 0
//    return { i++ }
//}
//
//fun main() {
//    val counter1 = makeCounter()
//    val counter2 = makeCounter()
//
//    println(counter1()) // 0
//    println(counter1()) // 1
//    println(counter2()) // 0
//    println(counter1()) // 2
//    println(counter1()) // 3
//    println(counter2()) // 1
//}

// Anonymous functions

//fun sum(a: Int, b: Int) = (a..b).fold(0, fun(acc: Int, i: Int): Int = acc + i)
//
//fun product(a: Int, b: Int) = (a..b).fold(1, fun(acc: Int, i: Int): Int = acc * i)

// Puzzler
//fun f1() {
//   (1..4).forEach {
//       if (it == 2) return
//       print(it)
//   }
//}
//
//fun f2() {
//   (1..4).forEach(fun(it) {
//       if (it == 2) return
//       print(it)
//   })
//}
//
//fun main(args: Array<String>) {
//   f1()
//   f2()
//}
