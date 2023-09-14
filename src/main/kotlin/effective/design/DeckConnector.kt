package creation

import org.junit.Test
import kotlin.test.assertTrue

class DeckConnector(val deckName: String) {
    var state: ConnectionState = ConnectionState.Initial

    // ...

    enum class ConnectionState { Initial, Connected, Disconnected }
}

class DeckConnectorTest {
    private val deck1 = DeckConnector("AAA")
    private val deck1copy = DeckConnector("AAA")
    private val deck2 = DeckConnector("BBB")
    private val deckConnected1 = DeckConnector("AAA").apply {
        state = DeckConnector.ConnectionState.Connected
    }
    private val deckConnected1copy = DeckConnector("AAA").apply {
        state = DeckConnector.ConnectionState.Connected
    }
    private val deckConnected2 = DeckConnector("BBB").apply {
        state = DeckConnector.ConnectionState.Connected
    }

    @Test
    fun equalityTest() {
        assertTrue(deck1 == deck1copy)
        assertTrue(deck1 != deck2)
        assertTrue(deckConnected1 == deckConnected1copy)
        assertTrue(deckConnected1 != deckConnected2)
    }

    @Test
    fun hashCodeTest() {
        assertTrue(deck1.hashCode() == deck1copy.hashCode())
        assertTrue(deck1.hashCode() != deck2.hashCode())
        assertTrue(deckConnected1.hashCode() == deckConnected1copy.hashCode())
        assertTrue(deckConnected1.hashCode() != deckConnected2.hashCode())

//        assertEquals(2001856, deck1.hashCode())
//        assertEquals(2032639, deck2.hashCode())
//        assertEquals(2001857, deckConnected1.hashCode())
//        assertEquals(2032640, deckConnected2.hashCode())
    }

//    @Test
//    fun comparisonTest() {
//        val decks = listOf(deck2, deckConnected2, deck1, deckConnected1)
//        val decksInOrder = listOf(deck1, deckConnected1, deck2, deckConnected2)
//        assertEquals(decksInOrder, decks.sorted())
//    }
}
