package advanced.reflection

import org.junit.Test
import kotlin.test.assertEquals

fun serializeToXml(value: Any): String = TODO()

fun main() {
    data class SampleDataClass(
        val externalTxnId: String,
        val merchantTxnId: String,
        val reference: String
    )

    val data = SampleDataClass(
        externalTxnId = "07026984141550752666",
        merchantTxnId = "07026984141550752666",
        reference = "MERCHPAY"
    )

    println(serializeToXml(data))
    // <SampleDataClass>
    //     <externalTxnId>07026984141550752666</externalTxnId>
    //     <merchantTxnId>07026984141550752666</merchantTxnId>
    //     <reference>MERCHPAY</reference>
    // </SampleDataClass>

    @SerializationNameMapper(UpperSnakeCaseName::class)
    @SerializationIgnoreNulls
    class Book(
        val title: String,
        val author: String,
        @SerializationName("YEAR")
        val publicationYear: Int,
        val isbn: String?,
        @SerializationIgnore
        val price: Double,
    )

    @SerializationNameMapper(UpperSnakeCaseName::class)
    class Library(
        val catalog: List<Book>
    )

    val library = Library(
        catalog = listOf(
            Book(
                title = "The Hobbit",
                author = "J. R. R. Tolkien",
                publicationYear = 1937,
                isbn = "978-0-261-10235-4",
                price = 9.99,
            ),
            Book(
                title = "The Witcher",
                author = "Andrzej Sapkowski",
                publicationYear = 1993,
                isbn = "978-0-575-09404-2",
                price = 7.99,
            ),
            Book(
                title = "Antifragile",
                author = "Nassim Nicholas Taleb",
                publicationYear = 2012,
                isbn = null,
                price = 12.99,
            )
        )
    )

    println(serializeToXml(library))
    // <LIBRARY>
    //     <CATALOG>
    //         <BOOK>
    //             <AUTHOR>J. R. R. Tolkien</AUTHOR>
    //             <ISBN>978-0-261-10235-4</ISBN>
    //             <YEAR>1937</YEAR>
    //             <TITLE>The Hobbit</TITLE>
    //         </BOOK>
    //         <BOOK>
    //             <AUTHOR>Andrzej Sapkowski</AUTHOR>
    //             <ISBN>978-0-575-09404-2</ISBN>
    //             <YEAR>1993</YEAR>
    //             <TITLE>The Witcher</TITLE>
    //         </BOOK>
    //        <BOOK>
    //            <AUTHOR>Nassim Nicholas Taleb</AUTHOR>
    //            <YEAR>2012</YEAR>
    //            <TITLE>Antifragile</TITLE>
    //        </BOOK>
    //     </CATALOG>
    // </LIBRARY>
}

class XmlSerializerTest {

    @Test
    fun `should serialize object with string`() {
        class ExampleClass(val s1: String, val s2: String)
        assertEquals(
            "<ExampleClass><s1>ABC</s1><s2>DEF</s2></ExampleClass>",
            serializeToXml(ExampleClass("ABC", "DEF"))
        )
    }

    @Test
    fun `should serialize nested objects`() {
        class Name(val value: String)
        class Box(val name: Name)
        assertEquals(
            "<Box><name><Name><value>ABC</value></Name></name></Box>",
            serializeToXml(Box(Name("ABC")))
        )
    }

    @Test
    fun `should serialize list`() {
        class ExampleClass(val names: List<String>, val grades: List<Int>)
        assertEquals(
            "<ExampleClass><grades>343</grades><names>ABC</names></ExampleClass>",
            serializeToXml(ExampleClass(listOf("A", "B", "C"), listOf(3, 4, 3)))
        )
    }

    @Test
    fun `should serialize map`() {
        class ExampleClass(val grades: Map<String, Int>)
        assertEquals(
            "<ExampleClass><grades><Alex>5</Alex><Beatrice>1</Beatrice></grades></ExampleClass>",
            serializeToXml(ExampleClass(mapOf("Alex" to 5, "Beatrice" to 1)))
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
            "<Creature><attack>2</attack><cost><ANY>3</ANY><FOREST>2</FOREST></cost><defence>4</defence><name>Cockatrice</name><traits>FLYING</traits></Creature>",
            serializeToXml(creature)
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
            "<Creature><attack>2</attack><cost><ANY>3</ANY><FOREST>2</FOREST></cost><defence>4</defence><traits>FLYING</traits></Creature>",
            serializeToXml(creature)
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
            "<Creature><att>2</att><cost><ANY>3</ANY><FOREST>2</FOREST></cost><def>4</def><name>Cockatrice</name><traits>FLYING</traits></Creature>",
            serializeToXml(creature)
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
            "<creature><element_cost><ANY>3</ANY><FOREST>2</FOREST></element_cost><is_special>true</is_special><is_user_already>false</is_user_already><long_name>Cockatrice</long_name><traits_list>FLYING</traits_list></creature>",
            serializeToXml(creature)
        )
    }

    @Test
    fun `should use property mapper`() {
        class Creature(
            @SerializationNameMapper(UpperSnakeCaseName::class)
            val longName: String,
            @SerializationNameMapper(SnakeCaseName::class)
            val traitsList: List<Trait>,
            @SerializationNameMapper(UpperSnakeCaseName::class)
            val elementCost: Map<Element, Int>,
            @SerializationNameMapper(SnakeCaseName::class)
            val isSpecial: Boolean,
            @SerializationNameMapper(UpperSnakeCaseName::class)
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
            "<Creature><ELEMENT_COST><ANY>3</ANY><FOREST>2</FOREST></ELEMENT_COST><is_special>true</is_special><IS_USER_ALREADY>false</IS_USER_ALREADY><LONG_NAME>Cockatrice</LONG_NAME><traits_list>FLYING</traits_list></Creature>",
            serializeToXml(creature)
        )
    }

    @Test
    fun `should override class mapper with property mapper`() {
        @SerializationNameMapper(UpperSnakeCaseName::class)
        class Creature(
            val longName: String,
            @SerializationNameMapper(SnakeCaseName::class)
            val traitsList: List<Trait>,
            val elementCost: Map<Element, Int>,
            @SerializationNameMapper(SnakeCaseName::class)
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
            "<CREATURE><ELEMENT_COST><ANY>3</ANY><FOREST>2</FOREST></ELEMENT_COST><is_special>true</is_special><IS_USED_ALREADY>false</IS_USED_ALREADY><LONG_NAME>Cockatrice</LONG_NAME><traits_list>FLYING</traits_list></CREATURE>",
            serializeToXml(creature)
        )
    }

    @Test
    fun `should override mappers with property name`() {
        @SerializationNameMapper(UpperSnakeCaseName::class)
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
            "<CREATURE><ELEMENT_COST><ANY>3</ANY><FOREST>2</FOREST></ELEMENT_COST><special>true</special><IS_USER_ALREADY>false</IS_USER_ALREADY><name>Cockatrice</name><TRAITS_LIST>FLYING</TRAITS_LIST></CREATURE>",
            serializeToXml(creature)
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
            "<CreatureIgnoringNulls><defence>4</defence><name>Cockatrice</name></CreatureIgnoringNulls>",
            serializeToXml(creatureIgnoring)
        )

        val creature = Creature(
            name = "Cockatrice",
            attack = null,
            defence = 4,
            extraDetails = null,
        )

        assertEquals(
            "<Creature><attack>null</attack><defence>4</defence><extraDetails>null</extraDetails><name>Cockatrice</name></Creature>",
            serializeToXml(creature)
        )
    }

    @Test
    fun `should produce correct result for sampe data class`() {
        data class SampleDataClass(
            val externalTxnId: String,
            val merchantTxnId: String,
            val reference: String
        )

        val data = SampleDataClass(
            externalTxnId = "07026984141550752666",
            merchantTxnId = "07026984141550752666",
            reference = "MERCHPAY"
        )

        assertEquals(
            "<SampleDataClass><externalTxnId>07026984141550752666</externalTxnId><merchantTxnId>07026984141550752666</merchantTxnId><reference>MERCHPAY</reference></SampleDataClass>",
            serializeToXml(data)
        )
    }

    @SerializationNameMapper(UpperSnakeCaseName::class)
    class Book(
        val title: String,
        val author: String,
        @SerializationName("YEAR")
        val publicationYear: Int,
        val isbn: String,
        @SerializationIgnore
        val price: Double,
    )

    @SerializationNameMapper(UpperSnakeCaseName::class)
    class Library(
        val catalog: List<Book>
    )

    @Test
    fun `should produce correct result for library salmpe`() {
        val library = Library(
            catalog = listOf(
                Book(
                    title = "The Hobbit",
                    author = "J. R. R. Tolkien",
                    publicationYear = 1937,
                    isbn = "978-0-261-10235-4",
                    price = 9.99,
                ),
                Book(
                    title = "The Witcher",
                    author = "Andrzej Sapkowski",
                    publicationYear = 1993,
                    isbn = "978-0-575-09404-2",
                    price = 7.99,
                ),
            )
        )

        assertEquals(
            "<LIBRARY><CATALOG><BOOK><AUTHOR>J. R. R. Tolkien</AUTHOR><ISBN>978-0-261-10235-4</ISBN><YEAR>1937</YEAR><TITLE>The Hobbit</TITLE></BOOK><BOOK><AUTHOR>Andrzej Sapkowski</AUTHOR><ISBN>978-0-575-09404-2</ISBN><YEAR>1993</YEAR><TITLE>The Witcher</TITLE></BOOK></CATALOG></LIBRARY>",
            serializeToXml(library)
        )
    }

    enum class Element {
        FOREST, ANY,
    }

    enum class Trait {
        FLYING
    }
}
