package examples.coroutines.hc1

import kotlin.*

fun main() {
    val l = buildList {
        repeat(3) {
            println("L: Adding User")
            add("User$it")
        }
    }

    val l2 = l.map {
        println("L: Processing")
        "Processed $it"
    }

    val s = sequence {
        repeat(3) {
            println("S: Adding User")
            yield("User$it")
        }
    }

    val s2 = s.map {
        println("S: Processing")
        "Processed $it"
    }
}
