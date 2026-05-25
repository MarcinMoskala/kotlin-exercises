package essentials.tree

import org.junit.Test
import kotlin.test.assertEquals

sealed class Tree {
    override fun toString(): String = when (this) {
        is Leaf -> value
        is Node -> "$left, $right"
    }
}

class Leaf(val value: String) : Tree()
class Node(val left: Tree, val right: Tree) : Tree()

class TreeTest {
    @Test
    fun testLeaf() {
        val tree = Leaf("A")
        assertEquals("A", tree.toString())
    }

    @Test
    fun testSimpleNode() {
        val tree = Node(Leaf("A"), Leaf("B"))
        assertEquals("A, B", tree.toString())
    }

    @Test
    fun testNestedNodes() {
        val tree = Node(
            Node(Leaf("A"), Leaf("B")),
            Leaf("C")
        )
        assertEquals("A, B, C", tree.toString())

        val tree2 = Node(
            Leaf("A"),
            Node(Leaf("B"), Leaf("C"))
        )
        assertEquals("A, B, C", tree2.toString())
    }

    @Test
    fun testComplexTree() {
        val tree = Node(
            Node(Leaf("A"), Node(Leaf("B"), Leaf("C"))),
            Node(Node(Leaf("D"), Leaf("E")), Leaf("F"))
        )
        assertEquals("A, B, C, D, E, F", tree.toString())
    }
}
