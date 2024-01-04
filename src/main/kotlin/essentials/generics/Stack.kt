package essentials.generics.stack

import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

// TODO

//fun main() {
//    val intStack = Stack<Int>()
//    intStack.push(1)
//    intStack.push(2)
//    intStack.push(3)
//
//    val stringStack = Stack<String>()
//    stringStack.push("A")
//    stringStack.push("B")
//    stringStack.push("C")
//
//    println(intStack.peek()) // 3
//    while (!intStack.isEmpty()) { // 3, 2, 1
//        println(intStack.pop())
//    }
//    println(intStack.peek()) // null
//    println(intStack.isEmpty()) // true
//
//    println(stringStack.size()) // 3
//    while (!stringStack.isEmpty()) { // C, B, A
//        println(stringStack.pop())
//    }
//}

class StackTest {

//    private lateinit var stack: Stack<Int>
//
//    @Before
//    fun setup() {
//        stack = Stack()
//    }
//
//    @Test
//    fun `when push is called, an element should be added to the stack`() {
//        stack.push(1)
//
//        assertEquals(1, stack.size())
//        assertEquals(1, stack.peek())
//    }
//
//    @Test
//    fun `when pop is called on a non-empty stack, the last element should be removed`() {
//        stack.push(1)
//        stack.push(2)
//
//        val poppedElement = stack.pop()
//
//        assertEquals(2, poppedElement)
//        assertEquals(1, stack.size())
//        assertEquals(1, stack.peek())
//    }
//
//    @Test
//    fun `when pop is called on an empty stack, it should return null`() {
//        val poppedElement = stack.pop()
//
//        assertNull(poppedElement)
//        assertTrue(stack.isEmpty())
//    }
//
//    @Test
//    fun `when peek is called, it should return the last element without removing it`() {
//        stack.push(1)
//        stack.push(2)
//
//        val peekedElement = stack.peek()
//
//        assertEquals(2, peekedElement)
//        assertEquals(2, stack.size())
//    }
//
//    @Test
//    fun `when peek is called on an empty stack, it should return null`() {
//        val peekedElement = stack.peek()
//
//        assertNull(peekedElement)
//    }
//
//    @Test
//    fun `when isEmpty is called on an empty stack, it should return true`() {
//        assertTrue(stack.isEmpty())
//    }
//
//    @Test
//    fun `when isEmpty is called on a non-empty stack, it should return false`() {
//        stack.push(1)
//
//        assertFalse(stack.isEmpty())
//    }
//
//    @Test
//    fun `size should accurately report the number of elements in the stack`() {
//        stack.push(1)
//        stack.push(2)
//        stack.push(3)
//
//        assertEquals(3, stack.size())
//    }
//
//    @Test
//    fun `consecutive pushes and pops should maintain stack integrity`() {
//        stack.push(1)
//        stack.push(2)
//        stack.push(3)
//
//        assertEquals(3, stack.pop())
//        assertEquals(2, stack.pop())
//        assertEquals(1, stack.pop())
//        assertTrue(stack.isEmpty())
//    }
//
//    @Test
//    fun `pushing null values should be supported`() {
//        val nullableStack: Stack<Int?> = Stack()
//
//        nullableStack.push(null)
//
//        assertNull(nullableStack.peek())
//        assertEquals(1, nullableStack.size())
//    }
//
//    @Test
//    fun `peek after multiple pushes should always return the last pushed element`() {
//        stack.push(1)
//        assertEquals(1, stack.peek())
//        stack.push(2)
//        assertEquals(2, stack.peek())
//        stack.push(3)
//        assertEquals(3, stack.peek())
//    }
}
