package examples.essentials

class User(val name: String) {
    fun cheer(): String {
        println("Hello, my name is $name")
        return "Hello, my name is $name"
    }
}

var user: User? = null

fun main() {
    val result = user?.cheer() // (does nothing)
    println(result) // null
    println(user?.name) // null
    
    user = User("Cookie")
    val result2 = user?.cheer() // Hello, my name is Cookie
    println(result2) // Hello, my name is Cookie
    println(user?.name) // Cookie
}

//fun check(str: String?) {
//    println("The value: \"$str\"")
//    println("The value or empty: \"${str.orEmpty()}\"")
//    println("Is it null or empty? " + str.isNullOrEmpty())
//    println("Is it null or blank? " + str.isNullOrBlank())
//}
//
//fun main() {
//    check("ABC")
//    // The value: "ABC"
//    // The value or empty: "ABC"
//    // Is it null or empty? false
//    // Is it null or blank? false
//    check(null)
//    // The value: "null"
//    // The value or empty: ""
//    // Is it null or empty? true
//    // Is it null or blank? true
//    check("")
//    // The value: ""
//    // The value or empty: ""
//    // Is it null or empty? true
//    // Is it null or blank? true
//    check("       ")
//    // The value: "       "
//    // The value or empty: "       "
//    // Is it null or empty? false
//    // Is it null or blank? true
//}

//fun check(list: List<Int>?) {
//    println("The list: \"$list\"")
//    println("The list or empty: \"${list.orEmpty()}\"")
//    println("Is it null or empty? " + list.isNullOrEmpty())
//}
//
//fun main() {
//    check(listOf(1, 2, 3))
//    // The list: "[1, 2, 3]"
//    // The list or empty: "[1, 2, 3]"
//    // Is it null or empty? false
//    check(null)
//    // The list: "null"
//    // The list or empty: "[]"
//    // Is it null or empty? true
//    check(listOf())
//    // The list: "[]"
//    // The list or empty: "[]"
//    // Is it null or empty? true
//}
