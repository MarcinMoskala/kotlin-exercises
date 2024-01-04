package advanced.reflection.jsonserializer

import org.junit.Test
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.test.assertEquals

fun serializeToJson(value: Any): String = TODO()

@SerializationNameMapper(SnakeCaseName::class)
@SerializationIgnoreNulls
class Creature(
    val name: String,
    @SerializationName("att")
    val attack: Int,
    @SerializationName("def")
    val defence: Int,
    val traits: List<Trait>,
    val elementCost: Map<Element, Int>,
    @SerializationNameMapper(LowerCaseName::class)
    val isSpecial: Boolean,
    @SerializationIgnore
    var used: Boolean = false,
    val extraDetails: String? = null,
)

object LowerCaseName : NameMapper {
    override fun map(name: String): String = name.lowercase()
}

class SnakeCaseName : NameMapper {
    val pattern = "(?<=.)[A-Z]".toRegex()

    override fun map(name: String): String =
        name.replace(pattern, "_$0").lowercase()
}

enum class Element {
    FOREST, ANY,
}

enum class Trait {
    FLYING
}

fun main() {
    val creature = Creature(
        name = "Cockatrice",
        attack = 2,
        defence = 4,
        traits = listOf(Trait.FLYING),
        elementCost = mapOf(
            Element.ANY to 3,
            Element.FOREST to 2
        ),
        isSpecial = true,
    )
    println(serializeToJson(creature))
    // {"att": 2, "def": 4,
    // "element_cost": {"ANY": 3, "FOREST": 2},
    // "isspecial": true, "name": "Cockatrice",
    // "traits": ["FLYING"]}
}

@Target(AnnotationTarget.PROPERTY)
annotation class SerializationName(val name: String)

@Target(AnnotationTarget.PROPERTY)
annotation class SerializationIgnore

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
annotation class SerializationNameMapper(val mapper: KClass<out NameMapper>)

@Target(AnnotationTarget.CLASS)
annotation class SerializationIgnoreNulls

interface NameMapper {
    fun map(name: String): String
}

class JsonSerializerTest {

    @Test
    fun `should serialize numbers`() {
        assertEquals("10", serializeToJson(10))
        assertEquals("123", serializeToJson(123))
    }

    @Test
    fun `should serialize string`() {
        assertEquals("\"ABC\"", serializeToJson("ABC"))
        assertEquals("\"A B C\"", serializeToJson("A B C"))
    }

    @Test
    fun `should serialize object with string`() {
        class ExampleClass(val s1: String, val s2: String)
        assertEquals(
            "{\"s1\": \"ABC\", \"s2\": \"DEF\"}",
            serializeToJson(ExampleClass("ABC", "DEF"))
        )
    }

    @Test
    fun `should serialize nested objects`() {
        class Name(val value: String)
        class Box(val name: Name)
        assertEquals(
            "{\"name\": {\"value\": \"ABC\"}}",
            serializeToJson(Box(Name("ABC")))
        )
    }

    @Test
    fun `should serialize list`() {
        class ExampleClass(val names: List<String>, val grades: List<Int>)
        assertEquals(
            "{\"grades\": [3, 4, 3], \"names\": [\"A\", \"B\", \"C\"]}",
            serializeToJson(ExampleClass(listOf("A", "B", "C"), listOf(3, 4, 3)))
        )
    }

    @Test
    fun `should serialize map`() {
        class ExampleClass(val grades: Map<String, Int>)
        assertEquals(
            "{\"grades\": {\"Alex\": 5, \"Beatrice\": 1}}",
            serializeToJson(ExampleClass(mapOf("Alex" to 5, "Beatrice" to 1)))
        )
    }

    @Test
    fun `should serialize complex object`() {
        class Creature(
            val name: String,
            val attack: Int,
            val defence: Int,
            val traits: List<Trait>,
            val cost: Map<Element, Int>,
        )

        val creature = Creature(
            name = "Cockatrice",
            attack = 2,
            defence = 4,
            traits = listOf(Trait.FLYING),
            cost = mapOf(
                Element.ANY to 3,
                Element.FOREST to 2
            )
        )
        assertEquals(
            "{\"attack\": 2, \"cost\": {\"ANY\": 3, \"FOREST\": 2}, \"defence\": 4, \"name\": \"Cockatrice\", \"traits\": [\"FLYING\"]}",
            serializeToJson(creature)
        )
    }

    @Test
    fun `should ignore properties`() {
        class Creature(
            @SerializationIgnore
            val name: String,
            val attack: Int,
            val defence: Int,
            val traits: List<Trait>,
            val cost: Map<Element, Int>,
        )

        val creature = Creature(
            name = "Cockatrice",
            attack = 2,
            defence = 4,
            traits = listOf(Trait.FLYING),
            cost = mapOf(
                Element.ANY to 3,
                Element.FOREST to 2
            )
        )
        assertEquals(
            "{\"attack\": 2, \"cost\": {\"ANY\": 3, \"FOREST\": 2}, \"defence\": 4, \"traits\": [\"FLYING\"]}",
            serializeToJson(creature)
        )
    }

    @Test
    fun `should use different property names`() {
        class Creature(
            val name: String,
            @SerializationName("att") val attack: Int,
            @SerializationName("def") val defence: Int,
            val traits: List<Trait>,
            val cost: Map<Element, Int>,
        )

        val creature = Creature(
            name = "Cockatrice",
            attack = 2,
            defence = 4,
            traits = listOf(Trait.FLYING),
            cost = mapOf(
                Element.ANY to 3,
                Element.FOREST to 2
            )
        )
        assertEquals(
            "{\"att\": 2, \"cost\": {\"ANY\": 3, \"FOREST\": 2}, \"def\": 4, \"name\": \"Cockatrice\", \"traits\": [\"FLYING\"]}",
            serializeToJson(creature)
        )
    }

    @Test
    fun `should use class mapper`() {
        @SerializationNameMapper(SnakeCaseName::class)
        class Creature(
            val longName: String,
            val traitsList: List<Trait>,
            val elementCost: Map<Element, Int>,
            val isSpecial: Boolean,
            var isUserAlready: Boolean = false,
        )

        val creature = Creature(
            longName = "Cockatrice",
            traitsList = listOf(Trait.FLYING),
            elementCost = mapOf(
                Element.ANY to 3,
                Element.FOREST to 2
            ),
            isSpecial = true,
        )
        assertEquals(
            "{\"element_cost\": {\"ANY\": 3, \"FOREST\": 2}, \"is_special\": true, \"is_user_already\": false, \"long_name\": \"Cockatrice\", \"traits_list\": [\"FLYING\"]}",
            serializeToJson(creature)
        )
    }

    @Test
    fun `should use property mapper`() {
        class Creature(
            @SerializationNameMapper(SnakeCaseName::class)
            val longName: String,
            @SerializationNameMapper(LowerCaseName::class)
            val traitsList: List<Trait>,
            @SerializationNameMapper(SnakeCaseName::class)
            val elementCost: Map<Element, Int>,
            @SerializationNameMapper(LowerCaseName::class)
            val isSpecial: Boolean,
            @SerializationNameMapper(SnakeCaseName::class)
            var isUserAlready: Boolean = false,
        )

        val creature = Creature(
            longName = "Cockatrice",
            traitsList = listOf(Trait.FLYING),
            elementCost = mapOf(
                Element.ANY to 3,
                Element.FOREST to 2
            ),
            isSpecial = true,
        )
        assertEquals(
            "{\"element_cost\": {\"ANY\": 3, \"FOREST\": 2}, \"isspecial\": true, \"is_user_already\": false, \"long_name\": \"Cockatrice\", \"traitslist\": [\"FLYING\"]}",
            serializeToJson(creature)
        )
    }

    @Test
    fun `should override class mapper with property mapper`() {
        @SerializationNameMapper(SnakeCaseName::class)
        class Creature(
            val longName: String,
            @SerializationNameMapper(LowerCaseName::class)
            val traitsList: List<Trait>,
            val elementCost: Map<Element, Int>,
            @SerializationNameMapper(LowerCaseName::class)
            val isSpecial: Boolean,
            var isUsedAlready: Boolean = false,
        )

        val creature = Creature(
            longName = "Cockatrice",
            traitsList = listOf(Trait.FLYING),
            elementCost = mapOf(
                Element.ANY to 3,
                Element.FOREST to 2
            ),
            isSpecial = true,
        )
        assertEquals(
            "{\"element_cost\": {\"ANY\": 3, \"FOREST\": 2}, \"isspecial\": true, \"is_used_already\": false, \"long_name\": \"Cockatrice\", \"traitslist\": [\"FLYING\"]}",
            serializeToJson(creature)
        )
    }

    @Test
    fun `should override mappers with property name`() {
        @SerializationNameMapper(SnakeCaseName::class)
        class Creature(
            @SerializationName("name")
            val longName: String,
            val traitsList: List<Trait>,
            val elementCost: Map<Element, Int>,
            @SerializationName("special")
            val isSpecial: Boolean,
            var isUserAlready: Boolean = false,
        )

        val creature = Creature(
            longName = "Cockatrice",
            traitsList = listOf(Trait.FLYING),
            elementCost = mapOf(
                Element.ANY to 3,
                Element.FOREST to 2
            ),
            isSpecial = true,
        )

        assertEquals(
            "{\"element_cost\": {\"ANY\": 3, \"FOREST\": 2}, \"special\": true, \"is_user_already\": false, \"name\": \"Cockatrice\", \"traits_list\": [\"FLYING\"]}",
            serializeToJson(creature)
        )
    }

    @Test
    fun `should ignore nulls if annotation used`() {
        @SerializationIgnoreNulls
        class CreatureIgnoringNulls(
            val name: String,
            val attack: Int?,
            val defence: Int?,
            val extraDetails: String?,
        )
        class Creature(
            val name: String,
            val attack: Int?,
            val defence: Int?,
            val extraDetails: String?,
        )

        val creatureIgnoring = CreatureIgnoringNulls(
            name = "Cockatrice",
            attack = null,
            defence = 4,
            extraDetails = null,
        )
        assertEquals(
            "{\"defence\": 4, \"name\": \"Cockatrice\"}",
            serializeToJson(creatureIgnoring)
        )

        val creature = Creature(
            name = "Cockatrice",
            attack = null,
            defence = 4,
            extraDetails = null,
        )

        assertEquals(
            "{\"attack\": null, \"defence\": 4, \"extraDetails\": null, \"name\": \"Cockatrice\"}",
            serializeToJson(creature)
        )
    }

    enum class Element {
        FOREST, ANY,
    }

    enum class Trait {
        FLYING
    }
}
