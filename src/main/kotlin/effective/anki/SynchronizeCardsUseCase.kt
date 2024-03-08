package anki

import anki.AnkiProgressBar.Size.Small

class SynchronizeCardsUseCase(
    private val view: AnkiView,
    private val networkRepository: AnkiNetworkRepository,
    private val cardsRepository: AnkiCardsRepository,
) {

    suspend fun start() {
        val progressBar = AnkiProgressBar(size = Small)
        view.show(progressBar)

        try {
            val cards = networkRepository.fetchCards()
            cardsRepository.updateCards(cards)
        } catch (e: AnkiApiException) {
            val dialog = AnkiDialog(
                title = "Cards synchronization exception",
                text = e.message,
                okButton = AnkiDialog.Button("OK")
            )
            view.show(dialog)
        } finally {
            view.hide(progressBar)
        }
    }
}
