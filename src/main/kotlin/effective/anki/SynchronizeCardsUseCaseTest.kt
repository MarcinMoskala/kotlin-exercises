package anki

import anki.fakes.FailingAnkiNetworkRepository
import anki.fakes.FakeAnkiView
import anki.fakes.InMemoryAnkiCardsRepository
import anki.fakes.InMemoryAnkiNetworkRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class SynchronizeCardsUseCaseTest {

    @Test
    fun `should synchronize cards`() = runTest {
        // given
        val view = FakeAnkiView()
        val cards = listOf(AnkiCard("A", "B"), AnkiCard("C", "D"))
        val cardsRepo = InMemoryAnkiCardsRepository()
        val networkRepo = InMemoryAnkiNetworkRepository(cards = cards)
        val useCase = SynchronizeCardsUseCase(view, networkRepo, cardsRepo)
        assertEquals(null, cardsRepo.updatedCards)

        // when
        useCase.start()

        // then
        assertEquals(cards, cardsRepo.updatedCards)
    }

    @Test
    fun `should show progress bar`() = runTest {
        // given
        val view = FakeAnkiView()
        val cards = listOf(AnkiCard("A", "B"), AnkiCard("C", "D"))
        val cardsRepo = InMemoryAnkiCardsRepository()
        val networkRepo = InMemoryAnkiNetworkRepository(cards = cards)
        val useCase = SynchronizeCardsUseCase(view, networkRepo, cardsRepo)
        assertEquals(0, view.visibleElements.filterIsInstance<AnkiProgressBar>().size)

        // when
        launch {
            useCase.start()
        }

        // then
        assertEquals(
            AnkiProgressBar(size = AnkiProgressBar.Size.Small),
            view.visibleElements.single { it is AnkiProgressBar })

        // when
        delay(1000)

        // then
        assertEquals(0, view.visibleElements.filterIsInstance<AnkiProgressBar>().size)
    }

    @Test
    fun `should show exceptions`() = runTest {
        // given
        val view = FakeAnkiView()
        val apiException = AnkiApiException(401, "User not found")
        val cardsRepo = InMemoryAnkiCardsRepository()
        val networkRepo = FailingAnkiNetworkRepository(apiException)
        val useCase = SynchronizeCardsUseCase(view, networkRepo, cardsRepo)
        assertEquals(0, view.visibleElements.filterIsInstance<AnkiDialog>().size)

        // when
        useCase.start()

        // then
        val dialog = view.visibleElements.single { it is AnkiDialog } as AnkiDialog
        assertEquals("Cards synchronization exception", dialog.title)
        assertEquals("User not found", dialog.text)
        assertEquals(AnkiDialog.Button("OK"), dialog.okButton)
        assertEquals(null, dialog.cancelButton)
    }
}
