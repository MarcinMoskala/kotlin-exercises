package request

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

/*
   TODO: This function should return the best student on the [semester].
 */
suspend fun getBestStudent(semester: String, repo: StudentsRepository): Student = TODO()

data class Student(val id: Int, val result: Double, val semester: String)

interface StudentsRepository {
    suspend fun getStudentIds(semester: String): List<Int>
    suspend fun getStudent(id: Int): Student
}

class RequestTest {

    @Test
    fun `Function does return the best student in the semester`() = runBlocking {
        // given
        val semester = "19L"
        val best = Student(2, 95.0, semester)
        val repo = ImmediateFakeStudentRepo(
            listOf(
                Student(1, 90.0, semester),
                best,
                Student(3, 50.0, semester)
            )
        )

        // when
        val chosen = getBestStudent(semester, repo)

        // then
        assertEquals(best, chosen)
    }

    @Test
    fun `When no students, correct error is thrown`() = runBlocking {
        // given
        val semester = "19L"
        val repo = ImmediateFakeStudentRepo(listOf())

        // when and then
        assertThrowsError<IllegalStateException> {
            getBestStudent(semester, repo)
        }
    }

    @Test
    fun `Requests do not wait for each other`() = runBlocking {
        // given
        val repo = WaitingFakeStudentRepo()

        // when and then
        assertTimeAround(1200) {
            getBestStudent("AAA", repo)
        }
    }

    @Test
    fun `Cancellation works fine`() = runBlocking {
        // given
        val repo = WaitingFakeStudentRepo()

        // when
        val job = launch {
            getBestStudent("AAA", repo)
        }
        delay(300)
        job.cancel()

        // then
        assertEquals(0, repo.returnedStudents)
    }

    @Test
    fun `When one request has error, all are stopped and error is thrown`() = runBlocking {
        // given
        val repo = FirstFailingFakeStudentRepo()

        // when and then
        assertThrowsError<FirstFailingFakeStudentRepo.FirstFailingError> {
            getBestStudent("AAA", repo)
        }

        // then
        assertEquals(
            0,
            repo.studentsReturned,
            "Looks like some requests were still running after the first one had an error"
        )
    }
}

class ImmediateFakeStudentRepo(
    private val students: List<Student>
) : StudentsRepository {

    override suspend fun getStudentIds(semester: String): List<Int> =
        students.filter { it.semester == semester }
            .map { it.id }

    override suspend fun getStudent(id: Int): Student =
        students.first { it.id == id }
}

inline fun assertTimeAround(expectedTime: Int, upperMargin: Int = 100, body: () -> Unit) {
    val actualTime = measureTimeMillis(body)
    assert(actualTime in expectedTime..(expectedTime + upperMargin)) {
        "Operation should take around $expectedTime, but it took $actualTime"
    }
}

inline fun <reified T : Throwable> assertThrowsError(body: () -> Unit) {
    try {
        body()
        assert(false) { "There should be an error of type ${T::class.simpleName}" }
    } catch (throwable: Throwable) {
        if (throwable !is T) {
            throw throwable
        }
    }
}

class WaitingFakeStudentRepo : StudentsRepository {
    var returnedStudents = 0

    override suspend fun getStudentIds(semester: String): List<Int> {
        delay(200)
        return (1..5).toList()
    }

    override suspend fun getStudent(id: Int): Student {
        delay(1000)
        returnedStudents++
        return Student(12, 12.0, "AAA")
    }
}

class FirstFailingFakeStudentRepo : StudentsRepository {
    var first = true
    var studentsReturned = 0
    val mutex = Mutex()

    override suspend fun getStudentIds(semester: String): List<Int> {
        delay(200)
        return (1..5).toList()
    }

    override suspend fun getStudent(id: Int): Student {
        delay(100)
        mutex.withLock {
            // To prevent more than one throwing
            if (first) {
                first = false
                throw FirstFailingError()
            }
        }
        delay(100)
        studentsReturned++
        return Student(12, 12.0, "AAA")
    }

    class FirstFailingError() : Error()
}
