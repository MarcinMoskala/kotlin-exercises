package coroutines.flow.updatenotesdatabaseusecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateNotesDatabaseUseCase(
    private val notesRemoteRepository: NotesRemoteRepository,
    private val notesLocalRepository: NotesLocalRepository,
) {
    fun updateNotes(): Flow<Progress> = TODO()
}

interface NotesRemoteRepository {
    suspend fun fetchNotesIds(): List<String>
    suspend fun getNote(id: String): Note
}

interface NotesLocalRepository {
    suspend fun saveNote(note: Note)
}

data class Note(
    val id: String,
    val content: String
)

data class Progress(
    val completed: Int,
    val total: Int,
)

class MockNotesRemoteRepository(
    private val notesToReturn: List<Note> = emptyList(),
    private val shouldThrowOnFetch: Boolean = false,
    private val shouldThrowOnGet: Boolean = false
) : NotesRemoteRepository {
    val fetchedNoteIds = mutableListOf<String>()
    var fetchNotesCallCount = 0
    var getNoteCallCount = 0

    override suspend fun fetchNotesIds(): List<String> {
        fetchNotesCallCount++
        if (shouldThrowOnFetch) throw RuntimeException("Fetch failed")
        return notesToReturn.map { it.id }
    }

    override suspend fun getNote(id: String): Note {
        getNoteCallCount++
        if (shouldThrowOnGet) throw RuntimeException("Get note failed")
        fetchedNoteIds.add(id)
        return notesToReturn.first { it.id == id }
    }
}

class MockNotesLocalRepository(
    private val shouldThrowOnSave: Boolean = false
) : NotesLocalRepository {
    val savedNotes = mutableListOf<Note>()

    override suspend fun saveNote(note: Note) {
        if (shouldThrowOnSave) throw RuntimeException("Save failed")
        savedNotes.add(note)
    }
}

// Tests
class UpdateNotesDatabaseUseCaseTest {

    @Test
    fun `updateNotes should emit progress for empty notes list`() = runTest {
        val remoteRepo = MockNotesRemoteRepository(emptyList())
        val localRepo = MockNotesLocalRepository()
        val useCase = UpdateNotesDatabaseUseCase(remoteRepo, localRepo)

        val progressList = useCase.updateNotes().toList()

        assertEquals(1, progressList.size)
        assertEquals(Progress(0, 0), progressList[0])
        assertEquals(0, localRepo.savedNotes.size)
    }

    @Test
    fun `updateNotes should emit correct progress for multiple notes`() = runTest {
        val notes = listOf(
            Note("1", "Note 1"),
            Note("2", "Note 2"),
            Note("3", "Note 3")
        )
        val remoteRepo = MockNotesRemoteRepository(notes)
        val localRepo = MockNotesLocalRepository()
        val useCase = UpdateNotesDatabaseUseCase(remoteRepo, localRepo)

        val progressList = useCase.updateNotes().toList()

        assertEquals(4, progressList.size)
        assertEquals(Progress(0, 3), progressList[0])
        assertEquals(Progress(1, 3), progressList[1])
        assertEquals(Progress(2, 3), progressList[2])
        assertEquals(Progress(3, 3), progressList[3])
    }

    @Test
    fun `updateNotes should save all notes to local repository`() = runTest {
        val notes = listOf(
            Note("1", "Note 1"),
            Note("2", "Note 2")
        )
        val remoteRepo = MockNotesRemoteRepository(notes)
        val localRepo = MockNotesLocalRepository()
        val useCase = UpdateNotesDatabaseUseCase(remoteRepo, localRepo)

        useCase.updateNotes().toList()

        assertEquals(2, localRepo.savedNotes.size)
        assertEquals(notes[0], localRepo.savedNotes[0])
        assertEquals(notes[1], localRepo.savedNotes[1])
        assertEquals(listOf("1", "2"), remoteRepo.fetchedNoteIds)
    }

    @Test
    fun `updateNotes should throw exception when fetchNotes fails`() = runTest {
        val remoteRepo = MockNotesRemoteRepository(shouldThrowOnFetch = true)
        val localRepo = MockNotesLocalRepository()
        val useCase = UpdateNotesDatabaseUseCase(remoteRepo, localRepo)

        assertFailsWith<RuntimeException> {
            useCase.updateNotes().toList()
        }
    }

    @Test
    fun `updateNotes should throw exception when getNote fails`() = runTest {
        val notes = listOf(Note("1", "Note 1"))
        val remoteRepo = MockNotesRemoteRepository(notes, shouldThrowOnGet = true)
        val localRepo = MockNotesLocalRepository()
        val useCase = UpdateNotesDatabaseUseCase(remoteRepo, localRepo)

        assertFailsWith<RuntimeException> {
            useCase.updateNotes().toList()
        }
    }

    @Test
    fun `updateNotes should throw exception when saveNote fails`() = runTest {
        val notes = listOf(Note("1", "Note 1"))
        val remoteRepo = MockNotesRemoteRepository(notes)
        val localRepo = MockNotesLocalRepository(shouldThrowOnSave = true)
        val useCase = UpdateNotesDatabaseUseCase(remoteRepo, localRepo)

        assertFailsWith<RuntimeException> {
            useCase.updateNotes().toList()
        }
    }

    @Test
    fun `updateNotes should handle single note correctly`() = runTest {
        val note = Note("single", "Single note content")
        val remoteRepo = MockNotesRemoteRepository(listOf(note))
        val localRepo = MockNotesLocalRepository()
        val useCase = UpdateNotesDatabaseUseCase(remoteRepo, localRepo)

        val progressList = useCase.updateNotes().toList()

        assertEquals(2, progressList.size)
        assertEquals(Progress(0, 1), progressList[0])
        assertEquals(Progress(1, 1), progressList[1])
        assertEquals(1, localRepo.savedNotes.size)
        assertEquals(note, localRepo.savedNotes[0])
        assertEquals(listOf("single"), remoteRepo.fetchedNoteIds)
    }

    @Test
    fun `updateNotes should handle notes with different content correctly`() = runTest {
        val notes = listOf(
            Note("id1", "First note with special chars: !@#$%"),
            Note("id2", ""),
            Note("id3", "Very long note content that spans multiple lines and contains various characters including unicode: 🚀 ✨ 💡")
        )
        val remoteRepo = MockNotesRemoteRepository(notes)
        val localRepo = MockNotesLocalRepository()
        val useCase = UpdateNotesDatabaseUseCase(remoteRepo, localRepo)

        val progressList = useCase.updateNotes().toList()

        assertEquals(4, progressList.size)
        assertEquals(Progress(0, 3), progressList[0])
        assertEquals(Progress(1, 3), progressList[1])
        assertEquals(Progress(2, 3), progressList[2])
        assertEquals(Progress(3, 3), progressList[3])
        assertEquals(3, localRepo.savedNotes.size)
        assertEquals(notes, localRepo.savedNotes)
    }

    @Test
    fun `updateNotes should handle large number of notes`() = runTest {
        val notes = (1..100).map { Note("id$it", "Content $it") }
        val remoteRepo = MockNotesRemoteRepository(notes)
        val localRepo = MockNotesLocalRepository()
        val useCase = UpdateNotesDatabaseUseCase(remoteRepo, localRepo)

        val progressList = useCase.updateNotes().toList()

        assertEquals(101, progressList.size) // Initial progress + 100 updates
        assertEquals(Progress(0, 100), progressList[0])
        assertEquals(Progress(50, 100), progressList[50])
        assertEquals(Progress(100, 100), progressList[100])
        assertEquals(100, localRepo.savedNotes.size)
        assertEquals(notes, localRepo.savedNotes)
    }

    @Test
    fun `updateNotes should maintain correct order of operations`() = runTest {
        val notes = listOf(
            Note("first", "First note"),
            Note("second", "Second note"),
            Note("third", "Third note")
        )
        val remoteRepo = MockNotesRemoteRepository(notes)
        val localRepo = MockNotesLocalRepository()
        val useCase = UpdateNotesDatabaseUseCase(remoteRepo, localRepo)

        useCase.updateNotes().toList()

        // Verify that notes were fetched in the correct order
        assertEquals(listOf("first", "second", "third"), remoteRepo.fetchedNoteIds)
        // Verify that notes were saved in the correct order
        assertEquals(notes, localRepo.savedNotes)
    }

    @Test
    fun `updateNotes should handle notes with duplicate IDs correctly`() = runTest {
        val notes = listOf(
            Note("duplicate", "First version"),
            Note("unique", "Unique note"),
            Note("duplicate", "Second version") // Same ID as first
        )
        val remoteRepo = MockNotesRemoteRepository(notes)
        val localRepo = MockNotesLocalRepository()
        val useCase = UpdateNotesDatabaseUseCase(remoteRepo, localRepo)

        val progressList = useCase.updateNotes().toList()

        assertEquals(4, progressList.size)
        assertEquals(Progress(0, 3), progressList[0])
        assertEquals(Progress(3, 3), progressList[3])
        assertEquals(3, localRepo.savedNotes.size)
        // Should fetch each note individually, even with duplicate IDs
        assertEquals(listOf("duplicate", "unique", "duplicate"), remoteRepo.fetchedNoteIds)
    }

    @Test
    fun `updateNotes should handle partial failure during processing`() = runTest {
        val notes = listOf(
            Note("1", "Note 1"),
            Note("2", "Note 2"),
            Note("3", "Note 3")
        )
        // Create a mock that fails on the second getNote call
        val remoteRepo = object : NotesRemoteRepository {
            private var getCallCount = 0
            override suspend fun fetchNotesIds() = notes.map { it.id }
            override suspend fun getNote(id: String): Note {
                getCallCount++
                if (getCallCount == 2) throw RuntimeException("Failed on second note")
                return notes.first { it.id == id }
            }
        }
        val localRepo = MockNotesLocalRepository()
        val useCase = UpdateNotesDatabaseUseCase(remoteRepo, localRepo)

        assertFailsWith<RuntimeException> {
            useCase.updateNotes().toList()
        }

        // Should have saved only the first note before failing
        assertEquals(1, localRepo.savedNotes.size)
        assertEquals(Note("1", "Note 1"), localRepo.savedNotes[0])
    }

    @Test
    fun `Progress data class should have correct properties`() {
        val progress = Progress(5, 10)
        assertEquals(5, progress.completed)
        assertEquals(10, progress.total)
    }

    @Test
    fun `Note data class should have correct properties`() {
        val note = Note("test-id", "test content")
        assertEquals("test-id", note.id)
        assertEquals("test content", note.content)
    }

    @Test
    fun `updateNotes should not call suspending functions more times than needed`() = runTest {
        val startTime = currentTime
        val notes = listOf(
            Note("1", "Note 1"),
            Note("2", "Note 2"),
            Note("3", "Note 3")
        )
        val remoteRepo = MockNotesRemoteRepository(notes)
        val localRepo = MockNotesLocalRepository()
        val useCase = UpdateNotesDatabaseUseCase(remoteRepo, localRepo)

        useCase.updateNotes().toList()

        // fetchNotes should be called exactly once
        assertEquals(1, remoteRepo.fetchNotesCallCount, "fetchNotes() should be called exactly once")

        // getNote should be called exactly once per note
        assertEquals(notes.size, remoteRepo.getNoteCallCount, "getNote() should be called exactly once per note")

        // Verify timing - operations should complete reasonably quickly
        val endTime = currentTime
        val duration = endTime - startTime
        println("[DEBUG_LOG] Test completed in ${duration}ms")
    }
}
