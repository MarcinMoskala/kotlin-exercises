package anki

import anki.AnkiProgressBar.Size.Small
import anki.fakes.FakeAnkiView
import anki.fakes.InMemoryAnkiCardsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals

class CorrectCardsUseCaseTest {

    @Test
    fun `should correct cards`() = runBlockingTest {
        // given
        val view = FakeAnkiView()
        val cardsRepo = InMemoryAnkiCardsRepository()
        val useCase = CorrectCardsUseCase(view, cardsRepo)
        assertEquals(false, cardsRepo.cardsCorrected)

        // when
        useCase.start()

        // then
        assertEquals(true, cardsRepo.cardsCorrected)
    }

    @Test
    fun `should show progress bar`() = runBlockingTest {
        // given
        val view = FakeAnkiView()
        val cardsRepo = InMemoryAnkiCardsRepository()
        val useCase = CorrectCardsUseCase(view, cardsRepo)
        assertEquals(0, view.visibleElements.filterIsInstance<AnkiProgressBar>().size)

        // when
        launch {
            useCase.start()
        }

        // then
        assertEquals(AnkiProgressBar(size = Small), view.visibleElements.single { it is AnkiProgressBar })

        // when
        delay(1000)

        // then
        assertEquals(0, view.visibleElements.filterIsInstance<AnkiProgressBar>().size)
    }

    @Test
    fun `should show dialog`() = runBlockingTest {
        // given
        val view = FakeAnkiView()
        val cardsRepo = InMemoryAnkiCardsRepository()
        val useCase = CorrectCardsUseCase(view, cardsRepo)
        assertEquals(0, view.visibleElements.filterIsInstance<AnkiDialog>().size)

        // when
        useCase.start()

        // then
        val dialog = view.visibleElements.single { it is AnkiDialog } as AnkiDialog
        assertEquals("Success", dialog.title)
        assertEquals("Cards correction successful", dialog.text)
        assertEquals(AnkiDialog.Button("OK"), dialog.okButton)
        assertEquals(null, dialog.cancelButton)
    }
}

