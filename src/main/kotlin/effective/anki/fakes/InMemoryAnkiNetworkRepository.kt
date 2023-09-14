package anki.fakes

import anki.AnkiCard
import anki.AnkiNetworkRepository
import anki.AnkiUser

class InMemoryAnkiNetworkRepository(
    private val cards: List<AnkiCard> = emptyList(),
    private val user: AnkiUser? = null
) : AnkiNetworkRepository {

    override suspend fun fetchCards(): List<AnkiCard> = cards

    override suspend fun fetchUser(): AnkiUser? = user
}
