package functional.base.inline

import org.junit.Test
import kotlin.reflect.typeOf
import kotlin.test.assertEquals

// TODO

//fun main() {
//    val list = listOf(1, "A", 3, "B")
//    println(list.anyOf<Int>()) // true
//    println(list.anyOf<String>()) // true
//    println(list.anyOf<Double>()) // true
//
//    println(list.firstOfOrNull<String>()) // A
//    println(list.firstOfOrNull<Int>()) // 1
//    println(list.firstOfOrNull<Double>()) // null
//
//    val map = mapOf(1 to 2, 2 to "A", 3 to 4, "B" to "C")
//    println(map.filterValuesInstanceOf<Int, String>())
//    // {2=A}
//    println(map.filterValuesInstanceOf<String, String>())
//    // {B=C}
//    println(map.filterValuesInstanceOf<Int, Int>())
//    // {1=2, 3=4}
//    println(map.filterValuesInstanceOf<String, Int>())
//    // {}
//}

class InlineTest {
//    @Test
//    fun anyOfTest() {
//        assert(listOf(1, 2, 3, "4").anyOf<Int>())
//        assert(listOf(1, 2, 3, "4").anyOf<String>())
//        assert(!listOf(1, 2, 3, "4").anyOf<Double>())
//        assert(listOf(1, 2, 3).anyOf<Int>())
//        assert(!listOf(1, 2, 3).anyOf<String>())
//        val o = object {
//            val result = listOf(1, 2, 3, "4").anyOf<Int>()
//        }
//        assertEquals(typeOf<Boolean>(), o::result.returnType)
//    }
//
//    @Test
//    fun firstOfOrNullTest() {
//        assertEquals(1, listOf(1, 2, 3, "4").firstOfOrNull<Int>())
//        assertEquals("4", listOf(1, 2, 3, "4").firstOfOrNull<String>())
//        assertEquals(null, listOf(1, 2, 3, "4").firstOfOrNull<Double>())
//        assertEquals(1, listOf(1, 2, 3).firstOfOrNull<Int>())
//        assertEquals(null, listOf(1, 2, 3).firstOfOrNull<String>())
//        val o = object {
//            val result = listOf(1, 2, 3, "4").firstOfOrNull<Int>()
//        }
//        assertEquals(typeOf<Int?>(), o::result.returnType)
//    }
//
//    @Test
//    fun filterValuesInstanceOfTest() {
//        assertEquals(
//            mapOf(1 to 1, 2 to 2, 3 to 3),
//            mapOf(1 to 1, 2 to 2, 3 to 3, 4 to "4").filterValuesInstanceOf<Int, Int>()
//        )
//        assertEquals(
//            mapOf(4 to "4"),
//            mapOf(1 to 1, 2 to 2, 3 to 3, 4 to "4").filterValuesInstanceOf<Int, String>()
//        )
//        assertEquals(
//            mapOf(),
//            mapOf(1 to 1, 2 to 2, 3 to 3, 4 to "4").filterValuesInstanceOf<String, String>()
//        )
//        assertEquals(
//            mapOf(1 to 1, 2 to 2, 3 to 3),
//            mapOf(1 to 1, 2 to 2, 3 to 3).filterValuesInstanceOf<Int, Int>()
//        )
//        assertEquals(
//            mapOf(),
//            mapOf(1 to 1, 2 to 2, 3 to 3).filterValuesInstanceOf<Int, String>()
//        )
//        val o = object {
//            val result = mapOf(1 to 1, 2 to 2, 3 to 3, 4 to "4").filterValuesInstanceOf<Int, Int>()
//        }
//        assertEquals(typeOf<Map<Int, Int>>(), o::result.returnType)
//    }
}
