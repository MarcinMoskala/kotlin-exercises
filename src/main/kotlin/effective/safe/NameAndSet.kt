package effective.safe.nameandset

fun main() {
    val set = mutableSetOf<Name>()
    val name = Name("AAA")
    set.add(name)
    // ???
    print(set.contains(name))
}

data class Name(var name: String)
