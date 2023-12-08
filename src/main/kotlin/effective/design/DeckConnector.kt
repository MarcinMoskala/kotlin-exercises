package creation

class DeckConnector(val deckName: String) {
    var state: ConnectionState = ConnectionState.Initial

    // ...

    enum class ConnectionState { Initial, Connected, Disconnected }
}