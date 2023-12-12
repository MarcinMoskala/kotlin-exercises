package effective.collections

import java.io.File
import kotlin.system.measureTimeMillis


fun main() {
    measureTimeMillis {
        File("Crimes_-_2001_to_Present.csv").readLines()
            .drop(1)
            .map { Crime.parse(it) }
            .groupBy { it.primaryType }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { (_, num) -> num }
            .joinToString(separator = "\n") { (type, num) ->
                "$num $type"
            }
            .let(::println)
    }.let { println("Took $it") }
}

class Crime(
    val id: String,
    val caseNumber: String,
    val date: String,
    val block: String,
    val iucr: String,
    val primaryType: String,

    ) {
    companion object {
        fun parse(line: String): Crime {
            val values = line.split(",")
            return Crime(
                id = values[0],
                caseNumber = values[1],
                date = values[2],
                block = values[3],
                iucr = values[4],
                primaryType = values[5]
            )
        }
    }
}
