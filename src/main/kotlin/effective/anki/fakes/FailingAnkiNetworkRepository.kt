package anki.fakes

import anki.AnkiApiException
import anki.AnkiCard
import anki.AnkiNetworkRepository
import anki.AnkiUser

class FailingAnkiNetworkRepository(
    private val exception: AnkiApiException
) : AnkiNetworkRepository {

    override suspend fun fetchCards(): List<AnkiCard> = throw exception

    override suspend fun fetchUser(): AnkiUser? = throw exception
}
