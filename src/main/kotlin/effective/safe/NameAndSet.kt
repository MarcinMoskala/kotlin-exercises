package effective.safe.nameandset

data class Name(var name: String)

fun main() {
    val set = mutableSetOf<Name>()
    val name = Name("AAA")
    set.add(name)
    // ???
    print(set.contains(name))
}
