fun main() {
    val a = ('a'..'z').toList()
    
    val n = 5
    println(a.drop(n))
    println(a.take(n))
    println(a.dropLast(n))
    println(a.takeLast(n))
}
