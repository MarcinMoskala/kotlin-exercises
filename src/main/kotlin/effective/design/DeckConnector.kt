package effective.design.deckconnector

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DeckConnector(
    val deckName: String
) : Comparable<DeckConnector> {
    var state: ConnectionState = ConnectionState.Initial

    override fun compareTo(other: DeckConnector): Int {
        TODO("Not yet implemented")
    }

    enum class ConnectionState {
        Initial,
        Connected,
        Disconnected
    }
}

class DeckConnectorTest {
    @Test
    fun `should be equal when deck name and state are equal`() {
        val deckConnector1 = DeckConnector("deck1")
        val deckConnector2 = DeckConnector("deck1")
        assertEquals(deckConnector1, deckConnector2)
    }

    @Test
    fun `should not equal when deck name or state are different`() {
        val deckConnector1 = DeckConnector("deck1")
        val deckConnector2 = DeckConnector("deck1")
        val deckConnector3 = DeckConnector("deck2")
        assertNotEquals(deckConnector1, deckConnector3)
        assertEquals(deckConnector1, deckConnector2)
        deckConnector1.state = DeckConnector.ConnectionState.Connected
        assertNotEquals(deckConnector1, deckConnector2)
        deckConnector2.state = DeckConnector.ConnectionState.Connected
        assertEquals(deckConnector1, deckConnector2)
    }

    @Test
    fun `should have equal hash code for equal objects`() {
        val deckConnector1 = DeckConnector("deck1")
        val deckConnector2 = DeckConnector("deck1")
        val deckConnector3 = DeckConnector("deck2")
        assertNotEquals(deckConnector1.hashCode(), deckConnector3.hashCode())
        assertEquals(deckConnector1.hashCode(), deckConnector2.hashCode())
        deckConnector1.state = DeckConnector.ConnectionState.Connected
        assertNotEquals(deckConnector1.hashCode(), deckConnector2.hashCode())
        deckConnector2.state = DeckConnector.ConnectionState.Connected
        assertEquals(deckConnector1.hashCode(), deckConnector2.hashCode())
    }

    @Test
    fun `should compare objects by deck name and state`() {
        val deck1 = DeckConnector("deck1").apply { state = DeckConnector.ConnectionState.Connected }
        val deck2 = DeckConnector("deck2").apply { state = DeckConnector.ConnectionState.Connected }
        val deck3 = DeckConnector("deck2").apply { state = DeckConnector.ConnectionState.Disconnected }
        val sortedDecks = listOf(
            deck1,
            DeckConnector("deck1").apply { state = DeckConnector.ConnectionState.Disconnected },
            deck2,
            deck3,
            DeckConnector("deck3").apply { state = DeckConnector.ConnectionState.Connected },
            DeckConnector("deck3").apply { state = DeckConnector.ConnectionState.Disconnected }
        )
        assertEquals(sortedDecks, sortedDecks.shuffled().sorted())
        assert(deck1 < deck2)
        assert(deck2 < deck3)
        assert(deck1 < deck3)
        assert(deck2 > deck1)
        assert(deck3 > deck2)
        assert(deck3 > deck1)
        assert(deck1 <= deck1)
        assert(deck2 <= deck2)
        assert(deck3 <= deck3)
    }
}
