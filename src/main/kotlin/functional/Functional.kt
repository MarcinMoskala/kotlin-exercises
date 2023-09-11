package functional

class FunctionsClassic {

    fun add(num1: Int, num2: Int): Int = num1 + num2

    fun printNum(num: Int) {
        print(num)
    }

    fun triple(num: Int): Int = num * 3

    fun longestOf(str1: String, str2: String, str3: String): String = listOf(str1, str2, str3)
        .maxByOrNull { it.length }!!
}

interface FunctionsFunctional {
    val add: Any
    val printNum: Any
    val triple: Any
    val longestOf: Any
}

class AnonymousFunctionalTypeSpecified : FunctionsFunctional {
    override val add: (Int, Int) -> Int = fun(num1, num2) = num1 + num2
    override val printNum = TODO()
    override val triple = TODO()
    override val longestOf = TODO()
}

class AnonymousFunctionalTypeInferred : FunctionsFunctional {
    override val add = fun(num1: Int, num2: Int) = num1 + num2
    override val printNum = TODO()
    override val triple = TODO()
    override val longestOf = TODO()
}

class LambdaFunctionalTypeSpecified : FunctionsFunctional {
    override val add: (Int, Int) -> Int = { num1, num2 -> num1 + num2 }
    override val printNum = TODO()
    override val triple = TODO()
    override val longestOf = TODO()
}

class LambdaFunctionalTypeInferred : FunctionsFunctional {
    override val add = { num1: Int, num2: Int -> num1 + num2 }
    override val printNum = TODO()
    override val triple = TODO()
    override val longestOf = TODO()
}

class FunctionReference : FunctionsFunctional {
    override val add: (Int, Int) -> Int = Int::plus
    override val printNum: (Int) -> Unit = TODO()
    override val triple: (Int) -> Int = TODO()
    override val longestOf: (String, String, String) -> String = TODO()
}

class FunctionMemberReference : FunctionsFunctional {
    override val add: (Int, Int) -> Int = this::add
    override val printNum: (Int) -> Unit = TODO()
    override val triple: (Int) -> Int = TODO()
    override val longestOf: (String, String, String) -> String = TODO()

    private fun add(num1: Int, num2: Int): Int = num1 + num2

    private fun printNum(num: Int) {
        print(num)
    }

    private fun triple(num: Int): Int = num * 3

    private fun longestOf(str1: String, str2: String, str3: String): String = listOf(str1, str2, str3)
        .maxByOrNull { it.length }!!
}

class BoundedFunctionReference : FunctionsFunctional {
    private val classic = FunctionsClassic()

    override val add: (Int, Int) -> Int = classic::add
    override val printNum: (Int) -> Unit = TODO()
    override val triple: (Int) -> Int = TODO()
    override val longestOf: (String, String, String) -> String = TODO()
}
