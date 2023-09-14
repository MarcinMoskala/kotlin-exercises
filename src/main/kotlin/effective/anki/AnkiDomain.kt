package anki

interface AnkiNetworkRepository {
    @Throws(AnkiApiException::class)
    suspend fun fetchCards(): List<AnkiCard>

    @Throws(AnkiApiException::class)
    suspend fun fetchUser(): AnkiUser?
}

interface AnkiCardsRepository {
    suspend fun updateCards(cards: List<AnkiCard>)
    suspend fun correctCards()
}

interface AnkiUserRepository {
    suspend fun updateUser(user: AnkiUser)
}

class AnkiCard(val front: String, val back: String)
class AnkiUser(val name: String)
class AnkiApiException(val code: Int, override val message: String): Throwable()
