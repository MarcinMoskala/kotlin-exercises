package functional

import functional.ProcessorCategory.*
import functional.collections.map.filter
import functional.collections.map.flatMap
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

//class Level(val value: Int) {
//    val fruitsNum = 5 + (value / 6)
//    val stepsNum = 3 + ((value + 3) / 6)
//}

fun main() {
    println("How many processing steps would you like? (1-10)")
    val level = readln().toInt()
    check(level in 0..10) { "Invalid level" }
    println("How many fruits would you like? (1-${fruits.size})")
    val fruitsNum = readln().toInt()
    check(fruitsNum in 0..(fruits.size)) { "Invalid level" }

    val challenge = generateCollectionProcessingChallenge(level, fruitsNum)

    val fruitPropertiesUsed = challenge.fruitPropertiesUsed()
    printFruitPropertiesTable(challenge.fruitsUsed, fruitPropertiesUsed)

    println(challenge.toDisplayString())

    println("What is the type of the result? (${resultTypes.keys.joinToString { it }})")
    val answerResultType = readln()
    check(answerResultType in resultTypes.keys) { "Invalid type" }
    if (answerResultType != challenge.resultType.classifierName) {
        println("Incorrect. The correct type is ${challenge.resultType.classifierName}")
        return
    }
    if (challenge.resultType.arguments.isNotEmpty()) {
        println("What is the type arguments?")
        val types = List(challenge.resultType.arguments.size) { index ->
            if (index == 0) println("First element:") else println("Second element:")
            readln()
        }
        if (types != challenge.resultType.arguments.map { it.type?.classifierName }) {
            println("Incorrect. The correct types are ${challenge.resultType.arguments.map { it.type?.classifierName }}")
            return
        }
    }
    println("Now what is the result?")
    val answer = readln()
    val correctAnswer = challenge.result.toString()
    if (answer != correctAnswer) {
        println("Incorrect. The correct answer is $correctAnswer")
        return
    }
    println("Correct!")
}

//fun main() {
    // Find dead ends
//    val deadEnds = processors.filter { processor ->
//        processors.none { processor.to.isSubtypeOf(it.from) } && !processor.to.itOneOfAllowedTerminalTypes
//    }.map { it.to }.distinct()
//    if (deadEnds.isNotEmpty()) {
//        println("Dead ends:")
//        deadEnds.forEach { println(it) }
//    } else {
//        println("No dead ends")
//    }
    
    // Collect all unique results
//    val results = mutableMapOf<KType, List<Any>>()
//    var resultsDisplay: Map<KType, List<Any>> = emptyMap()
//    repeat(10) {
//        repeat(10000) {
//            val challenge = generateCollectionProcessingChallenge(Random.nextInt(8))
//            val result = challenge.resultType
//            results[result] = (results[result] ?: emptyList()) + challenge.result
//        }
//        val newResultsDisplay = results.mapValues { (k, v) ->
//            when {
//                k.isSubtypeOf(typeOf<Iterable<*>>()) -> (v as List<List<Any>>).flatten()
//                k.isSubtypeOf(typeOf<Map<*, *>>()) -> (v as List<Map<Any, Any>>).flatMap { it.toList() }
//                else -> v
//            }.distinct()
//        }
//        if (newResultsDisplay != resultsDisplay) {
//            resultsDisplay = newResultsDisplay
//            println("Results:")
//            resultsDisplay.forEach { (k, v) ->
//                println("$k: $v")
//            }
//        } else {
//            println("No new results")
//        }
//    }
//}

private fun printFruitPropertiesTable(fruitsUsed: Set<Fruit>, fruitPropertiesUsed: List<KProperty1<Fruit, *>>) {
    if (fruitsUsed.isEmpty()) return
    if (fruitPropertiesUsed.isEmpty()) return
    println()
    println("Fruit properties:")
    val padSizes = listOf(3) + fruitPropertiesUsed.map {
        fruitsUsed.map { it }.maxOf { fruit -> it.get(fruit).toString().length + 1 }
    }
    print("".padEnd(padSizes[0]))
    fruitPropertiesUsed.forEachIndexed { i, it -> print(it.name.padEnd(padSizes[i + 1])) }
    println()
    fruitsUsed.forEach { fruit ->
        print(fruit.icon.padEnd(padSizes[0]))
        fruitPropertiesUsed.forEachIndexed { i, prop ->
            print(prop.get(fruit).toString().padEnd(padSizes[i + 1]))
        }
        println()
    }
    println()
    println()
}

fun generateCollectionProcessingChallenge(steps: Int, fruitsNum: Int): CollectionProcessingChallenge {
    val start = List(fruitsNum) { fruits.random() }
    var fruitsUsed = start.toSet()
    var result: Any = start
    var resultType: KType = typeOf<List<Fruit>>()
    val chosenProcessors = mutableListOf<Processor>()
    repeat(steps) {
        val isLastStep = it == steps - 1
        val availableProcessors = processors.filter {
            resultType.isSubtypeOf(it.from) &&
                    it !in chosenProcessors &&
                    (!isLastStep || it.to.itOneOfAllowedTerminalTypes) &&
                    it.category !in chosenProcessors.takeLast(3).map { it.category }
        }
        if (availableProcessors.isEmpty()) {
//            println("No available processors for $resultType")
            return generateCollectionProcessingChallenge(steps, fruitsNum)
        }
        val processor = availableProcessors.random()
        try {
            result = processor.process(result) as Any
        } catch (e: Exception) {
//            println("Error processing $processor")
//            e.printStackTrace()
            return generateCollectionProcessingChallenge(steps, fruitsNum)
        }
        resultType = processor.to
        chosenProcessors.add(processor)
    }
    return CollectionProcessingChallenge(start, chosenProcessors, result, resultType, fruitsUsed)
}

data class CollectionProcessingChallenge(
    val start: List<Fruit>,
    val steps: List<Processor>,
    val result: Any,
    val resultType: KType,
    val fruitsUsed: Set<Fruit>
) {
    fun toDisplayString() = buildString {
        appendLine(start)
        steps.forEach { appendLine("   ." + it.display) }
    }

    fun fruitPropertiesUsed(): List<KProperty1<Fruit, *>> {
        val stepsAsString = steps.joinToString { it.display }
        return Fruit::class.memberProperties
            .filter { it.name in stepsAsString }
    }
}

val processors = listOf(
    processor<Iterable<Fruit>, List<Fruit>>("filter { it.price > 5.0 }", Filter) { it.filter { it.price > 5.0 } },
    processor<Iterable<Fruit>, List<Fruit>>("filter { it.price >= 10.0 }", Filter) { it.filter { it.price >= 10.0 } },
    processor<Iterable<Fruit>, List<Fruit>>("filter { it.price < 15.0 }", Filter) { it.filter { it.price < 15.0 } },
    processor<Iterable<Fruit>, List<Fruit>>("filter { it.price <= 7.0 }", Filter) { it.filter { it.price <= 7.0 } },
    processor<Iterable<Fruit>, List<String>>("map { it.name }", Map) { it.map { it.name } },
    processor<Iterable<Fruit>, List<Color>>("map { it.color }", Map) { it.map { it.color } },
    processor<Iterable<Fruit>, List<Double>>("map { it.price }", Map) { it.map { it.price } },
    processor<Iterable<Fruit>, Set<Fruit>>("toSet()") { it.toSet() },
    processor<Iterable<Fruit>, Map<Fruit, Color>>("associateWith { it.color }") { it.associateWith { it.color } },
    processor<Iterable<Fruit>, Map<Color, Fruit>>("associateBy { it.color }") { it.associateBy { it.color } },
    processor<Iterable<Fruit>, Map<Color, List<Fruit>>>("groupBy { it.color }") { it.groupBy { it.color } },
    processor<Iterable<Fruit>, List<Fruit>>("distinct()", Distinct) { it.distinct() },
    processor<Iterable<Fruit>, List<Fruit>>("sortedBy { it.price }", Sort) { it.sortedBy { it.price } },
    processor<Iterable<Fruit>, List<Fruit>>("sortedByDescending { it.price }", Sort) { it.sortedByDescending { it.price } },
    processor<Iterable<Fruit>, List<Fruit>>("sortedBy { it.name }", Sort) { it.sortedBy { it.name } },
    processor<Iterable<Fruit>, List<Fruit>>("sortedByDescending { it.name }", Sort) { it.sortedByDescending { it.name } },
    processor<Iterable<Fruit>, List<Fruit>>("distinctBy { it.color }", Distinct) { it.distinctBy { it.color } },
    processor<Iterable<Fruit>, Double>("maxOf { it.price }") { it.maxOf { it.price } },
    processor<Iterable<Fruit>, Double>("minOf { it.price }") { it.minOf { it.price } },
    processor<Iterable<Fruit>, Fruit>("maxBy { it.price }") { it.maxBy { it.price } },
    processor<Iterable<Fruit>, Fruit>("minBy { it.price }") { it.minBy { it.price } },
    processor<Iterable<Fruit>, List<Fruit>>("take(4)") { it.take(4) },
    processor<List<Fruit>, List<Fruit>>("takeLast(4)") { it.takeLast(4) },
    processor<Iterable<Fruit>, List<Fruit>>("drop(2)") { it.drop(2) },
    processor<List<Fruit>, List<Fruit>>("dropLast(2)") { it.dropLast(2) },
    processor<Iterable<Fruit>, _>("zipWithNext { f1, f2 -> listOf(f1, f2).maxBy { it.price } }") { it.zipWithNext { f1, f2 -> listOf(f1, f2).maxBy { it.price } } },
    processor<Iterable<Fruit>, _>("zipWithNext().toMap()") { it.zipWithNext().toMap() },
    processor<Iterable<Fruit>, _>("zipWithNext { f1, f2 -> f1 to f2.price }.toMap()") { it.zipWithNext { f1, f2 -> f1 to f2.price }.toMap() },
    
    processor<Collection<List<Fruit>>, _>("flatten()") { it.flatten() },
    
    // List<Color>
    processor<Iterable<Color>, List<String>>("map { it.name }", Map) { it.map { it.name } },
    processor<Iterable<Color>, List<Color>>("distinct()", Distinct) { it.distinct() },
    processor<Iterable<Color>, List<Color>>("take(4)") { it.take(4) },
    processor<List<Color>, List<Color>>("takeLast(4)") { it.takeLast(4) },
    processor<Iterable<Color>, List<Color>>("drop(2)") { it.drop(2) },
    processor<List<Color>, List<Color>>("dropLast(2)") { it.dropLast(2) },
    processor<Iterable<Color>, Color>("first()") { it.first() },
    processor<Iterable<Color>, Color>("last()") { it.last() },
    processor<Iterable<Color>, Boolean>("any { it == Color.Red }") { it.any { it == Color.Red } },
    processor<Iterable<Color>, Boolean>("none { it == Color.Red }") { it.none { it == Color.Red } },
    processor<Iterable<Color>, Boolean>("any { it == Color.Green }") { it.any { it == Color.Green } },
    processor<Iterable<Color>, Boolean>("none { it == Color.Green }") { it.none { it == Color.Green } },
    processor<Iterable<Color>, Boolean>("all { it == Color.Red }") { it.all { it == Color.Red } },
    processor<Iterable<Color>, Int>("count { it == Color.Red }") { it.count { it == Color.Red } },
    processor<Iterable<Color>, List<Boolean>>("map { it == Color.Red }", Map) { it.map { it == Color.Red } },
    processor<Iterable<Color>, List<Boolean>>("map { it == Color.Green }", Map) { it.map { it == Color.Green } },

    // List<Double>    
    processor<Iterable<Double>, List<Int>>("map { it.toInt() }", Map) { it.map { it.toInt() } },
    processor<Iterable<Double>, List<Double>>("map { it.toInt() }", Map) { it.filter { it >= 3.0 } },
    processor<Iterable<Double>, List<Double>>("sorted()", Sort) { it.sorted() },
    processor<Iterable<Double>, Iterable<Double>>("take(4)") { it.take(4) },
    processor<List<Double>, Iterable<Double>>("takeLast(4)") { it.takeLast(4) },
    processor<Iterable<Double>, Iterable<Double>>("drop(2)") { it.drop(2) },
    processor<List<Double>, Iterable<Double>>("dropLast(2)") { it.dropLast(2) },
    
    // List<Boolean>
    processor<Iterable<Boolean>, Int>("count { it }") { it.count { it } },
    processor<Iterable<Boolean>, Int>("count { !it }") { it.count { !it } },
    processor<Iterable<Boolean>, Boolean>("any { it }") { it.any { it } },
    processor<Iterable<Boolean>, Boolean>("none { it }") { it.none { it } },
    processor<Iterable<Boolean>, Boolean>("all { it }") { it.all { it } },
    processor<Iterable<Boolean>, List<Boolean>>("map { !it }", Map) { it.map { !it } },
    
    // Iterable<Int>
    processor<Iterable<Int>, List<Int>>("filter { it > 6 }", Filter) { it.filter { it > 6 } },
    processor<Iterable<Int>, List<Int>>("filter { it < 6 }", Filter) { it.filter { it < 6 } },
    processor<Iterable<Int>, List<Int>>("filter { it % 2 == 0 }", Filter) { it.filter { it % 2 == 0 } },
    processor<Iterable<Int>, List<Int>>("filter { it % 2 == 1 }", Filter) { it.filter { it % 2 == 1 } },
    processor<Iterable<Int>, List<Int>>("sorted()", Sort) { it.sorted() },
    processor<Iterable<Int>, List<Int>>("sortedDescending()", Sort) { it.sortedDescending() },
    processor<Iterable<Int>, Int>("max()") { it.max() },
    processor<Iterable<Int>, Int>("min()") { it.min() },
    processor<Iterable<Int>, Int>("sum()") { it.sum() },
    
    processor<Iterable<*>, Int>("count()") { it.count() },
    
    processor<Iterable<List<Fruit>>, List<Fruit>>("flatten()") { it.flatten() },
    
    // Map<Fruit, Color>
    processor<Map<Fruit, Color>, Set<Fruit>>("keys") { it.keys },
    processor<Map<Fruit, Color>, Collection<Color>>("values") { it.values },
    processor<Map<Fruit, Color>, _>("filter { it.key.color != Color.Red }", Filter) { it.filter { it.key.color != Color.Red } },
    processor<Map<Fruit, Color>, _>("filter { it.value == Color.Red }", Filter) { it.filter { it.value == Color.Red } },
    processor<Map<Fruit, Color>, _>("mapKeys { it.key.color }", Filter) { it.mapKeys { it.key.color } },
    processor<Map<Fruit, Color>, _>("map { it.key.color }", Filter) { it.map { it.key.color } },
    processor<Map<Fruit, Color>, _>("mapValues { it.key.price }", Filter) { it.mapValues { it.key.price } },
    
    // Map<Color, Fruit>
    processor<Map<Color, Fruit>, Set<Color>>("keys") { it.keys },
    processor<Map<Color, Fruit>, Collection<Fruit>>("values") { it.values },
    processor<Map<Color, Fruit>, _>("filter { it.key.color != Color.Red }") { it.filter { it.key != Color.Red } },
    processor<Map<Color, Fruit>, _>("filter { it.value.color == Color.Red }") { it.filter { it.value.color == Color.Red } },
    
    // Map<Color, List<Fruit>>
    processor<Map<Color, List<Fruit>>, Set<Color>>("keys") { it.keys },
    processor<Map<Color, List<Fruit>>, Collection<List<Fruit>>>("values") { it.values },
    processor<Map<Color, List<Fruit>>, _>("filter { it.key.color != Color.Red }") { it.filter { it.key != Color.Red } },
    processor<Map<Color, List<Fruit>>, _>("filter { it.value.any { it.color == Color.Red } }") { it.filter { it.value.any { it.color == Color.Red } } },
    
    // Map<Color, List<Fruit>>
    processor<Map<Color, List<Fruit>>, Map<Color, Int>>("mapValues { it.value.size }") { it.mapValues { it.value.size } },
    processor<Map<Color, List<Fruit>>, Map<Color, Fruit>>("mapValues { it.value.maxBy { it.price } }") { it.mapValues { it.value.maxBy { it.price } } },
    processor<Map<Color, List<Fruit>>, Map<Color, Fruit>>("mapValues { it.value.minBy { it.price } } }") { it.mapValues { it.value.minBy { it.price } } },
    processor<Map<Color, List<Fruit>>, Map<Color, Double>>("mapValues { it.value.maxOf { it.price } }") { it.mapValues { it.value.maxOf { it.price } } },
    processor<Map<Color, List<Fruit>>, Map<Color, Double>>("mapValues { it.value.minOf { it.price } } ") { it.mapValues { it.value.minOf { it.price } } },
    
    // Map<Color, Int>
    processor<Map<Color, Int>, Map<Color, Int>>("filter { it.value > 2 }", Filter) { it.filter { it.value > 2 } },
    processor<Map<Color, Int>, Map<Color, Int>>("filter { it.value < 2 }", Filter) { it.filter { it.value < 2 } },
    processor<Map<Color, Int>, Map<Color, Int>>("filter { it.value % 2 == 0 }", Filter) { it.filter { it.value % 2 == 0 } },
    processor<Map<Color, Int>, Map<Color, Int>>("filter { it.value % 2 == 1 }", Filter) { it.filter { it.value % 2 == 1 } },
    processor<Map<Color, Int>, Set<Color>>("keys") { it.keys },
    processor<Map<Color, Int>, Collection<Int>>("values") { it.values },
    
    // List<String>
    processor<Iterable<String>, List<Int>>("map { it.length }", Map) { it.map { it.length } },
    processor<Iterable<String>, List<String>>("sorted()", Sort) { it.sorted() },
    processor<Iterable<String>, List<String>>("sortedDescending()", Sort) { it.sortedDescending() },
    processor<Iterable<String>, List<String>>("sortedBy { it.length }", Sort) { it.sortedBy { it.length } },
    processor<Iterable<String>, List<String>>("sortedByDescending { it.length }", Sort) { it.sortedByDescending { it.length } },
    processor<Iterable<String>, List<String>>("distinct()", Distinct) { it.distinct() },
    processor<Iterable<String>, List<String>>("take(4)") { it.take(4) },
    processor<List<String>, List<String>>("takeLast(4)") { it.takeLast(4) },
    processor<Iterable<String>, List<String>>("drop(2)") { it.drop(2) },
    processor<List<String>, List<String>>("dropLast(2)") { it.dropLast(2) },
    
    // Map<Fruit, Fruit>
    processor<Map<Fruit, Fruit>, Set<Fruit>>("keys") { it.keys },
    processor<Map<Fruit, Fruit>, Collection<Fruit>>("values") { it.values },
    processor<Map<Fruit, Fruit>, Map<Fruit, Double>>("mapValues { it.value.price }") { it.mapValues { it.value.price } },
    processor<Map<Fruit, Fruit>, Map<Fruit, Color>>("mapValues { it.value.color }") { it.mapValues { it.value.color } },
    
    // Map<Fruit, Double>
    processor<Map<Fruit, Double>, Set<Fruit>>("keys") { it.keys },
    processor<Map<Fruit, Double>, Collection<Double>>("values") { it.values },
    processor<Map<Fruit, Double>, Map<Fruit, Double>>("filter { it.value < 12.0 }", Filter) { it.filter { it.value < 12.0 } },
    processor<Map<Fruit, Double>, Map<Fruit, Double>>("filter { it.value > 4.0 }", Filter) { it.filter { it.value > 4.0 } },
    
    
    // Map<Color, Color>
    processor<Map<Color, Color>, Set<Color>>("keys") { it.keys },
    processor<Map<Color, Color>, Collection<Color>>("values") { it.values },
    processor<Map<Color, Color>, Map<Color, Color>>("filter { it.key != it.value }", Filter) { it.filter { it.key != it.value } },
    processor<Map<Color, Color>, Map<Color, Color>>("filter { it.key == it.value }", Filter) { it.filter { it.key == it.value } },
    
    // Map<Color, Double>
    processor<Map<Color, Double>, Set<Color>>("keys") { it.keys },
    processor<Map<Color, Double>, Collection<Double>>("values") { it.values },
    processor<Map<Color, Double>, Map<Color, Double>>("filter { it.value < 12.0 }", Filter) { it.filter { it.value < 12.0 } },
    processor<Map<Color, Double>, Map<Color, Double>>("filter { it.value > 4.0 }", Filter) { it.filter { it.value > 4.0 } },
)

val resultTypes = processors.map { it.to }.distinct().associateBy { it.classifierName }

val KType.classifierName: String
    get() = (classifier as? KClass<*>)?.simpleName.orEmpty()

val KType.itOneOfAllowedTerminalTypes  
    get() = when {
        this in basicSupportedTypes -> true
        this.classifier == List::class || this.classifier == Set::class -> arguments.first().let { it.type in basicSupportedTypes }
        else -> false
    }

val basicSupportedTypes = setOf(typeOf<Fruit>(), typeOf<Color>(), typeOf<Int>(), typeOf<Double>(), typeOf<String>(), typeOf<Boolean>())

val fruits = listOf(
    Fruit("🍎", "Apple", 2.0, Color.Red),
    Fruit("🍌", "Banana", 2.5, Color.Yellow),
    Fruit("🍇", "Grape", 3.0, Color.Red),
    Fruit("🍊", "Orange", 3.0, Color.Orange),
    Fruit("🍏", "Green Apple", 1.5, Color.Green),
    Fruit("🍆", "Eggplant", 4.0, Color.Purple),
    Fruit("🍉", "Watermelon", 5.0, Color.Red),
    Fruit("🍑", "Peach", 3.0, Color.Orange),
    Fruit("🍓", "Strawberry", 6.0, Color.Red),
    Fruit("🍍", "Pineapple", 6.0, Color.Yellow),
    Fruit("🥝", "Kiwi", 4.0, Color.Green),
    Fruit("🍒", "Cherry", 8.0, Color.Red),
    Fruit("🥭", "Mango", 9.0, Color.Orange),
    Fruit("🍋", "Lemon", 8.0, Color.Yellow),
    Fruit("🍈", "Melon", 9.0, Color.Green),
    Fruit("🍐", "Pear", 3.0, Color.Green),
    Fruit("🥑", "Avocado", 22.0, Color.Green),
    Fruit("🍅", "Tomato", 4.0, Color.Red),
    Fruit("🫒", "Olive", 14.0, Color.Green),
    Fruit("🥦", "Broccoli", 5.0, Color.Green),
    Fruit("🌶️", "Pepper", 43.0, Color.Red),
    Fruit("🫑", "Bell Pepper", 7.0, Color.Red),
    Fruit("🌽", "Corn", 5.0, Color.Yellow),
    Fruit("🥕", "Carrot", 4.0, Color.Orange),
    Fruit("🍠", "Sweet Potato", 9.0, Color.Orange),
)

data class Fruit(
    val icon: String,
    val name: String,
    val price: Double,
    val color: Color,
) {
    override fun toString(): String = icon
}

enum class Color {
    Red, Yellow, Purple, Orange, Green
}

inline fun <reified T, reified R> processor(
    display: String,
    category: ProcessorCategory? = null,
    noinline process: (T) -> R
): Processor = Processor(
    display = display,
    from = typeOf<T>(),
    to = typeOf<R>(),
    process = { process(it as T) },
    category = category
)


data class Processor(
    val display: String,
    val from: KType,
    val to: KType,
    val process: (Any?) -> Any?,
    val category: ProcessorCategory?
)

enum class ProcessorCategory {
    Filter, Map, Sort, Distinct
}
