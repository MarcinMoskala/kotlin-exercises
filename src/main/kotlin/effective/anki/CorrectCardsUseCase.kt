package anki

import anki.AnkiProgressBar.Size.Small

class CorrectCardsUseCase(
    private val view: AnkiView,
    private val cardsRepository: AnkiCardsRepository,
) {

    suspend fun start() {
        val progressBar = AnkiProgressBar(size = Small)
        view.show(progressBar)

        try {
            cardsRepository.correctCards()
        } finally {
            view.hide(progressBar)
        }

        val dialog = AnkiDialog(
            title = "Success",
            text = "Cards correction successful",
            okButton = AnkiDialog.Button("OK"),
        )
        view.show(dialog)
    }
}
