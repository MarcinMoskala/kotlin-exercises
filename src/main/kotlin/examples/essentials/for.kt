package examples.essentials

fun main() {
    val animals = listOf("cat", "dog", "cow")
    for (animal in animals) {
        println("Animal: $animal")
    }
    
//    val linkedList = LinkedList(1, LinkedList(2, LinkedList(3)))
//    for (value in linkedList) {
//        println("Value: $value")
//    }
    
//    for (i in 1..10) {
//        println("i: $i")
//    }
    
//    val names = listOf("Alex", "Bob", "Celina")
//
//    for (i in names.indices) {
//        val name = names[i]
//        println("[$i] $name")
//    }
    
//    val capitals = mapOf(
//        "USA" to "Washington DC",
//        "Poland" to "Warsaw",
//        "Ukraine" to "Kiev"
//    )
//
//    for (entry in capitals.entries) {
//        println("The capital of ${entry.key} is ${entry.value}")
//    }
    
//    for (i in 1..5) {
//       if (i == 3) break
//       print(i)
//    }
//    // 12
//    
//    println()
//    
//    for (i in 1..5) {
//       if (i == 3) continue
//       print(i)
//    }
//    // 1245
    
}

class LinkedList(
    val value: Int,
    val next: LinkedList? = null
): Iterable<Int> {
    override fun iterator(): Iterator<Int> = object : Iterator<Int> {
        var current: LinkedList? = this@LinkedList
        override fun hasNext(): Boolean = current != null
        override fun next(): Int {
            val value = current?.value ?: throw NoSuchElementException()
            current = current?.next
            return value
        }
    }
}
