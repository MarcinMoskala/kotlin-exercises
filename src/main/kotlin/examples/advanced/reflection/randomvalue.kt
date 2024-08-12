import kotlin.math.ln
import kotlin.random.Random
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.withNullability
import kotlin.reflect.typeOf

class RandomValueConfig(
    val nullProbability: Double = 0.1,
    val zeroProbability: Double = 0.1,
    val stringSizeParam: Double = 0.1,
    val listSizeParam: Double = 0.3,
)

class ValueGenerator(
    private val random: Random = Random,
    val config: RandomValueConfig = RandomValueConfig(),
) {

    inline fun <reified T> randomValue(): T =
        randomValue(typeOf<T>()) as T
    
    fun randomValue(type: KType): Any? = when {
        type.isMarkedNullable -> randomNullable(type)
        type == typeOf<Boolean>() -> randomBoolean()
        type == typeOf<Int>() -> randomInt()
        type == typeOf<String>() -> randomString()
        type.isSubtypeOf(typeOf<List<*>>()) ->
            randomList(type)
        // ...
        else -> error("Type $type not supported")
    }
    
    private fun randomNullable(type: KType) =
        if (randomBoolean(config.nullProbability)) null
        else randomValue(type.withNullability(false))
    
    private fun randomString(): String =
        (1..random.exponential(config.stringSizeParam))
            .map { CHARACTERS.random(random) }
            .joinToString(separator = "")
    
    private fun randomList(type: KType) =
        List(random.exponential(config.listSizeParam)) {
            randomValue(type.arguments[0].type!!)
        }
    
    private fun randomInt() =
        if (randomBoolean(config.zeroProbability)) 0
        else random.nextInt()
    
    private fun randomBoolean() =
        random.nextBoolean()
    
    private fun randomBoolean(probability: Double) =
        random.nextDouble() < probability
    
    companion object {
        private val CHARACTERS =
            ('A'..'Z') + ('a'..'z') + ('0'..'9') + " "
    }
}

private fun Random.exponential(f: Double): Int {
    return (ln(1 - nextDouble()) / -f).toInt()
}

fun main() {
    val r = Random(1)
    val g = ValueGenerator(random = r)
    println(g.randomValue<Int>()) // -527218591
    println(g.randomValue<Int?>()) // -2022884062
    println(g.randomValue<Int?>()) // null
    println(g.randomValue<List<Int>>())
    // [-1171478239]
    println(g.randomValue<List<List<Boolean>>>())
    // [[true, true, false], [], [], [false, false], [],
    // [true, true, true, true, true, true, true, false]]
    println(g.randomValue<List<Int?>?>())
    // [-416634648, null, 382227801]
    println(g.randomValue<String>()) // WjMNxTwDPrQ
    println(g.randomValue<List<String?>>()) // [VAg, , null, AIKeGp9Q7, 1dqARHjUjee3i6XZzhQ02l, DlG, , ]
}
