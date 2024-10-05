package effective.safe.nameandset

fun main() {
    val set = mutableSetOf<Name>()
    val name = Name("AAA")
    set.add(name)
    // ???
    println(set.contains(name)) // should print false
    println(set.first() == name) // should print true
}

data class Name(var name: String)
