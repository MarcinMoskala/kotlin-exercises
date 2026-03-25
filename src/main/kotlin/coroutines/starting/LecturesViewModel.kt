package coroutines.starting.lecturesviewmodel

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class LecturesViewModel(
    private val lecturesRepository: LecturesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LecturesUiState(loading = true))
    val uiState: StateFlow<LecturesUiState> = _uiState.asStateFlow()

    init {
        TODO()
    }

    fun onToggleFavorite(lectureId: String) {
        TODO()
    }
}

interface LecturesRepository {
    suspend fun getPresentations(): List<Presentation>
    suspend fun getUserFavorites(): List<String>
    suspend fun setUserFavorite(lectureId: String, isFavorite: Boolean)
}

data class LecturesUiState(
    val loading: Boolean = false,
    val lectures: List<PresentationUi> = emptyList(),
)

data class Presentation(
    val id: String,
    val title: String,
    val speaker: String,
)

fun Presentation.toUi(isFavorite: Boolean): PresentationUi = PresentationUi(
    id = id,
    title = title,
    speaker = speaker,
    isFavorite = isFavorite
)

data class PresentationUi(
    val id: String,
    val title: String,
    val speaker: String,
    val isFavorite: Boolean,
)

abstract class ViewModel() {
    protected val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun onCleared() {
        viewModelScope.cancel()
    }
}

class PresentationsViewModelTest {
    private lateinit var scheduler: TestCoroutineScheduler
    private lateinit var viewModel: LecturesViewModel
    private lateinit var repository: FakeLecturesRepository

    @Before
    fun setUp() {
        scheduler = TestCoroutineScheduler()
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
        repository = FakeLecturesRepository()
        viewModel = LecturesViewModel(repository)
    }

    @Test
    fun `initial state should show loading`() {
        // Initial state should show loading
        assertEquals(true, viewModel.uiState.value.loading)

        // Run initial coroutines
        scheduler.runCurrent()

        // Should still be loading while coroutines are in progress
        assertEquals(true, viewModel.uiState.value.loading)

        // Complete the coroutines
        scheduler.advanceUntilIdle()

        // Loading should be false after operations complete
        assertEquals(false, viewModel.uiState.value.loading)
    }

    @Test
    fun `should load lectures and user favorites`() {
        // Setup test data
        val lecture1 = Presentation("1", "Kotlin Coroutines", "John Doe")
        val lecture2 = Presentation("2", "Flow API", "Jane Smith")
        repository.setPresentations(listOf(lecture1, lecture2))
        repository.setFavorites(listOf("1"))

        // Run all coroutines
        scheduler.advanceUntilIdle()

        // Verify lectures are loaded with correct favorite status
        val lectures = viewModel.uiState.value.lectures
        assertEquals(2, lectures.size)

        val pres1 = lectures.find { it.id == "1" }
        val pres2 = lectures.find { it.id == "2" }

        assertEquals(true, pres1?.isFavorite)
        assertEquals(false, pres2?.isFavorite)
        assertEquals(false, viewModel.uiState.value.loading) // Loading should be false after loading completes
    }

    @Test
    fun `should toggle favorite status`() {
        // Setup test data
        val lecture = Presentation("1", "Kotlin Coroutines", "John Doe")
        repository.setPresentations(listOf(lecture))
        repository.setFavorites(listOf())

        // Run initial load
        scheduler.advanceUntilIdle()

        // Verify initial state
        val initialPres = viewModel.uiState.value.lectures.first()
        assertEquals(false, initialPres.isFavorite)

        // Toggle favorite
        viewModel.onToggleFavorite("1")
        scheduler.runCurrent()

        // Verify repository was called with correct parameters
        assertEquals("1", repository.lastFavoriteId)
        assertEquals(true, repository.lastFavoriteState)

        // Complete the coroutine
        scheduler.advanceUntilIdle()

        // Verify lecture was updated
        val updatedPres = viewModel.uiState.value.lectures.first()
        assertEquals(true, updatedPres.isFavorite)
    }

    @Test
    fun `getPresentations and getUserFavorites should be fetched asynchronously`() {
        // Setup test data
        val lecture = Presentation("1", "Kotlin Coroutines", "John Doe")
        repository.setPresentations(listOf(lecture))
        repository.setFavorites(listOf("1"))

        scheduler.advanceUntilIdle()

        // Execution time of getPresentations and getUserFavorites should be 100ms,
        // as defined in the FakePresentationsRepository, so if they are called asynchronously,
        // the current time should be 100ms after the initial call.
        assertEquals(100, scheduler.currentTime)
    }
}

class FakeLecturesRepository : LecturesRepository {
    private var lectures = listOf<Presentation>()
    private var favorites = listOf<String>()
    var lastFavoriteId: String? = null
    var lastFavoriteState: Boolean? = null
    var shouldThrowException = false
    var getPresentationsIsRunning = false
        private set
    var getUserFavoritesIsRunning = false
        private set

    fun setPresentations(newPresentations: List<Presentation>) {
        lectures = newPresentations
    }

    fun setFavorites(newFavorites: List<String>) {
        favorites = newFavorites
    }

    override suspend fun getPresentations(): List<Presentation> {
        if (shouldThrowException) {
            throw RuntimeException("Test exception")
        }
        getPresentationsIsRunning = true
        try {
            delay(100)
        } finally {
            getPresentationsIsRunning = false
        }
        return lectures
    }

    override suspend fun getUserFavorites(): List<String> {
        if (shouldThrowException) {
            throw RuntimeException("Test exception")
        }
        getUserFavoritesIsRunning = true
        try {
            delay(100)
        } finally {
            getUserFavoritesIsRunning = false
        }
        return favorites
    }

    override suspend fun setUserFavorite(lectureId: String, isFavorite: Boolean) {
        lastFavoriteId = lectureId
        lastFavoriteState = isFavorite

        if (isFavorite) {
            favorites = favorites + lectureId
        } else {
            favorites = favorites.filter { it != lectureId }
        }
    }
}
