package functional.base

import org.junit.Test
import kotlin.reflect.KClass
import kotlin.reflect.typeOf
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FunctionsClassic {

    fun add(num1: Int, num2: Int): Int = num1 + num2

    fun printNum(num: Int) {
        print(num)
    }

    fun triple(num: Int): Int = num * 3

    fun produceName(name: String): Name = Name(name)

    fun longestOf(
        str1: String,
        str2: String,
        str3: String,
    ): String =
        maxOf(str1, str2, str3, compareBy { it.length })
}

data class Name(val name: String)

class AnonymousFunctionalTypeSpecified {
    val add: (Int, Int) -> Int = fun(num1, num2) = num1 + num2

    // TODO: Implement printNum, triple, produceName and longestOf properties using anonymous functions
    //  their type should be specified explicitly
    //  See add property for example
    val printNum: (Int) -> Unit = fun(num) = print(num)
    val triple: (Int) -> Int = fun(num) = num * 3
    val produceName: (String) -> Name = fun(name) = Name(name)
    val longestOf: (String, String, String) -> String = fun(str1, str2, str3) =
        maxOf(str1, str2, str3, compareBy { it.length })
}

class AnonymousFunctionalTypeInferred {
    val add = fun(num1: Int, num2: Int) = num1 + num2

    // TODO: Implement printNum, triple, produceName and longestOf properties using anonymous functions
    //  their type should be inferred by compiler
    //  See add property for example
    val printNum = fun(num: Int) = print(num)
    val triple = fun(num: Int) = num * 3
    val produceName = fun(name: String) = Name(name)
    val longestOf = fun(str1: String, str2: String, str3: String) =
        maxOf(str1, str2, str3, compareBy { it.length })
}

class LambdaFunctionalTypeSpecified {
    val add: (Int, Int) -> Int = { num1, num2 -> num1 + num2 }

    // TODO: Implement printNum, triple, produceName and longestOf properties using lambda functions
    //  their type should be specified explicitly
    //  See add property for example
    val printNum: (Int) -> Unit = { num -> print(num) }
    val triple: (Int) -> Int = { num -> num * 3 }
    val produceName: (String) -> Name = { name -> Name(name) }
    val longestOf: (String, String, String) -> String = { str1, str2, str3 ->
        maxOf(str1, str2, str3, compareBy { it.length })
    }
}

class LambdaFunctionalTypeInferred {
    val add = { num1: Int, num2: Int -> num1 + num2 }

    // TODO: Implement printNum, triple, produceName and longestOf properties using lambda functions
    //  their type should be inferred by compiler
    //  See add property for example
    val printNum = { num: Int -> print(num) }
    val triple = { num: Int -> num * 3 }
    val produceName = { name: String -> Name(name) }
    val longestOf = { str1: String, str2: String, str3: String ->
        maxOf(str1, str2, str3, compareBy { it.length })
    }
}

class FunctionReference {
    val add: (Int, Int) -> Int = Int::plus

    // TODO: Implement printNum, triple and produceName properties using function references
    //  to functions from the Kotlin stdlib or from the Name class
    //  See add property for example
    val printNum: (Int) -> Unit = ::print
    val triple: (Int) -> Int = 3::times
    val produceName: (String) -> Name = ::Name
}

class FunctionMemberReference {
    val add: (Int, Int) -> Int = this::add

    // TODO: Implement printNum, triple, produceName and longestOf properties using function references
    //  to functions from the current class
    //  See add property for example
    val printNum: (Int) -> Unit = this::printNum
    val triple: (Int) -> Int = this::triple
    val produceName: (String) -> Name = this::produceName
    val longestOf: (String, String, String) -> String = this::longestOf

    private fun add(num1: Int, num2: Int): Int = num1 + num2

    private fun printNum(num: Int) {
        print(num)
    }

    private fun triple(num: Int): Int = num * 3
    
    private fun produceName(name: String): Name = Name(name)

    private fun longestOf(
        str1: String, 
        str2: String, 
        str3: String
    ): String = 
        maxOf(str1, str2, str3, compareBy { it.length })
}

class BoundedFunctionReference {
    private val classic = FunctionsClassic()

    val add: (Int, Int) -> Int = classic::add

    // TODO: Implement printNum, triple, produceName and longestOf properties using function references
    //  to functions from the `classic` object
    //  See add property for example
    val printNum: (Int) -> Unit = classic::printNum
    val triple: (Int) -> Int = classic::triple
    val produceName: (String) -> Name = classic::produceName
    val longestOf: (String, String, String) -> String = classic::longestOf
}

class FunctionalTest {

    @Test
    fun `AnonymousFunctionalTypeSpecified has correct property signatures`() {
        checkPropertySignatures(AnonymousFunctionalTypeSpecified::class)
    }

    @Test
    fun `AnonymousFunctionalTypeSpecified has correct property behavior`() {
        checkPropertyBehavior(AnonymousFunctionalTypeSpecified())
    }

    @Test
    fun `AnonymousFunctionalTypeInferred has correct property signatures`() {
        checkPropertySignatures(AnonymousFunctionalTypeInferred::class)
    }

    @Test
    fun `AnonymousFunctionalTypeInferred has correct property behavior`() {
        checkPropertyBehavior(AnonymousFunctionalTypeInferred())
    }

    @Test
    fun `LambdaFunctionalTypeSpecified has correct property signatures`() {
        checkPropertySignatures(LambdaFunctionalTypeSpecified::class)
    }

    @Test
    fun `LambdaFunctionalTypeSpecified has correct property behavior`() {
        checkPropertyBehavior(LambdaFunctionalTypeSpecified())
    }

    @Test
    fun `LambdaFunctionalTypeInferred has correct property signatures`() {
        checkPropertySignatures(LambdaFunctionalTypeInferred::class)
    }

    @Test
    fun `LambdaFunctionalTypeInferred has correct property behavior`() {
        checkPropertyBehavior(LambdaFunctionalTypeInferred())
    }

    @Test
    fun `FunctionReference has correct property signatures`() {
        checkPropertySignatures(FunctionReference::class, expectLongestOf = false)
    }

    @Test
    fun `FunctionReference has correct property behavior`() {
        checkPropertyBehavior(FunctionReference(), expectLongestOf = false)
    }

    @Test
    fun `FunctionMemberReference has correct property signatures`() {
        checkPropertySignatures(FunctionMemberReference::class)
    }

    @Test
    fun `FunctionMemberReference has correct property behavior`() {
        checkPropertyBehavior(FunctionMemberReference())
    }

    @Test
    fun `BoundedFunctionReference has correct property signatures`() {
        checkPropertySignatures(BoundedFunctionReference::class)
    }

    @Test
    fun `BoundedFunctionReference has correct property behavior`() {
        checkPropertyBehavior(BoundedFunctionReference())
    }

    private fun checkPropertySignatures(
        classToCheck: KClass<*>,
        expectLongestOf: Boolean = true,
    ) {
        val c = classToCheck.members
        val properties = mutableMapOf(
            "add" to typeOf<(Int, Int) -> Int>(),
            "printNum" to typeOf<(Int) -> Unit>(),
            "triple" to typeOf<(Int) -> Int>(),
            "produceName" to typeOf<(String) -> Name>(),
        )
        if (expectLongestOf) {
            properties += "longestOf" to typeOf<(String, String, String) -> String>()
        }
        for ((propertyName, propertyType) in properties) {
            val propertyReference = c.find { it.name == propertyName }
            assertNotNull(propertyReference) { "Property $propertyName is missing" }
            assertEquals(propertyType, propertyReference.returnType, "Property $propertyName has wrong type")
        }
    }

    private fun <T: Any> checkPropertyBehavior(
        instance: T,
        expectLongestOf: Boolean = true,
    ) {
        val members = instance::class.members
        val add = members.find { it.name == "add" }!!
        assertEquals(3, (add.call(instance) as (Int, Int) -> Int)(1, 2))
        assertEquals(12, (add.call(instance) as (Int, Int) -> Int)(4, 8))
        val printNum = members.find { it.name == "printNum" }!!
        (printNum.call(instance) as (Int) -> Unit)(42)
        val triple = members.find { it.name == "triple" }!!
        assertEquals(9, (triple.call(instance) as (Int) -> Int)(3))
        assertEquals(15, (triple.call(instance) as (Int) -> Int)(5))
        val produceName = members.find { it.name == "produceName" }!!
        assertEquals(Name("John"), (produceName.call(instance) as (String) -> Name)("John"))
        assertEquals(Name("Jane"), (produceName.call(instance) as (String) -> Name)("Jane"))
        if (expectLongestOf) {
            val longestOf = members.find { it.name == "longestOf" }!!
            assertEquals("abc", (longestOf.call(instance) as (String, String, String) -> String)("a", "ab", "abc"))
            assertEquals("xyz", (longestOf.call(instance) as (String, String, String) -> String)("x", "xy", "xyz"))
        }
    }
}
