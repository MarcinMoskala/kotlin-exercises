package effective.collections

import java.io.File
import kotlin.system.measureTimeMillis


fun main() {
    measureTimeMillis {
        // Download CSV data from: https://data.cityofchicago.org/Public-Safety/Crimes-2001-to-Present/ijzp-q8t2/data
        File("Crimes_-_2001_to_Present.csv").readLines()
            .drop(1)
            .map { it.split(",")[5] }
//           TODO: Count the number of crimes per each primary cause
//            .sortedByDescending { (_, num) -> num }
//            .joinToString(separator = "\n") { (type, num) -> "$num $type" }
            .let(::println)
    }.let { println("Took $it") }
}
