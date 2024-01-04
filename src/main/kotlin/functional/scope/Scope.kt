package functional.scope.scope

import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Test
import java.time.LocalDateTime
import java.util.*

class StudentService(
    private val studentRepository: StudentRepository,
    private val studentFactory: StudentFactory,
    private val logger: Logger,
) {
    fun addStudent(
        addStudentRequest: AddStudentRequest
    ): Student? {
        val student = studentFactory
            .produceStudent(addStudentRequest)
            ?: return null
        studentRepository.addStudent(student)
        return student
    }

    fun getStudent(studentId: String): ExposedStudent? {
        val student = studentRepository.getStudent(studentId)
            ?: return null
        
        logger.log("Student found: $student")
        return studentFactory.produceExposed(student)
    }

    fun getStudents(semester: String): List<ExposedStudent> {
        val request = produceGetStudentsRequest(semester)
        val students = studentRepository.getStudents(request)
        logger.log("${students.size} students in $semester")
        return students
            .map { studentFactory.produceExposed(it) }
    }

    private fun produceGetStudentsRequest(
        semester: String,
    ): GetStudentsRequest {
        val request = GetStudentsRequest()
        request.expectedSemester = semester
        request.minResult = 3.0
        return request
    }
}

class StudentFactory(
    private val timeProvider: TimeProvider,
    private val uuidGenerator: UuidGenerator,
) {
    fun produceExposed(student: Student): ExposedStudent = ExposedStudent(
        name = student.name,
    )

    fun produceStudent(student: AddStudentRequest): Student? = Student(
        name = student.name,
        semester = student.semester,
        result = student.result,
        creationTime = timeProvider.now(),
        id = uuidGenerator.generate(),
    )
}

class StudentServiceTest {
    private val logger = FakeLogger()
    private val userRepository = FakeStudentRepository()
    private val timeProvider = FakeTimeProvider()
    private val uuidGenerator = FakeUuidGenerator()
    private val studentFactory = StudentFactory(timeProvider, uuidGenerator)
    private val studentService = StudentService(userRepository, studentFactory, logger)

    @After
    fun cleanup() {
        logger.cleanup()
        userRepository.cleanup()
        timeProvider.cleanup()
        uuidGenerator.cleanup()
    }

    @Test
    fun `should add student`() {
        // Given
        val time1 = LocalDateTime.of(2023, 5, 6, 7, 8)
        timeProvider.now = time1
        val studentId = "1234"
        uuidGenerator.constantUuid = studentId
        val addStudentRequest = AddStudentRequest(
            name = "Marc",
            semester = "1",
            result = 4.0,
        )

        // When
        val result = studentService.addStudent(addStudentRequest)

        // Then
        val expected = Student(
            name = "Marc",
            semester = "1",
            result = 4.0,
            creationTime = time1,
            id = studentId,
        )
        assertEquals(expected, result)
        assertEquals(expected, userRepository.getStudent(studentId))
    }

    @Test
    fun `should get student`() {
        // Given
        val studentId = "1234"
        uuidGenerator.constantUuid = studentId
        val addStudentRequest = AddStudentRequest(
            name = "Marc",
            semester = "1",
            result = 4.0,
        )
        studentService.addStudent(addStudentRequest)

        // When
        val result = studentService.getStudent(studentId)

        // Then
        val expected = ExposedStudent(
            name = "Marc",
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should log when getting student`() {
        // Given
        val time1 = LocalDateTime.of(2023, 5, 6, 7, 8)
        timeProvider.now = time1
        val studentId = "1234"
        uuidGenerator.constantUuid = studentId
        val addStudentRequest = AddStudentRequest(
            name = "Marc",
            semester = "1",
            result = 4.0,
        )
        studentService.addStudent(addStudentRequest)

        // When
        val result = studentService.getStudent(studentId)

        // Then
        val expected = listOf(
            "Student found: Student(id=1234, name=Marc, creationTime=2023-05-06T07:08, semester=1, result=4.0)"
        )
        assertEquals(expected, logger.messages)
    }

    @Test
    fun `should return null when getting non-existing student`() {
        // Given
        val studentId = "1234"
        uuidGenerator.constantUuid = studentId
        val addStudentRequest = AddStudentRequest(
            name = "Marc",
            semester = "1",
            result = 4.0,
        )
        studentService.addStudent(addStudentRequest)

        // When
        val result = studentService.getStudent("1")

        // Then
        assertEquals(null, result)
    }

    @Test
    fun `should get students list and log its size`() {
        // Given
        listOf(
            AddStudentRequest("Alex", "Semester1", 4.0),
            AddStudentRequest("Bob", "Semester2", 4.0),
            AddStudentRequest("Celine", "Semester1", 2.0),
            AddStudentRequest("Diana", "Semester1", 5.0),
            AddStudentRequest("Ester", "Semester2", 3.0),
            AddStudentRequest("Fiona", "Semester1", 4.0),
            AddStudentRequest("Gina", "Semester2", 2.9),
            AddStudentRequest("Helen", "Semester1", 3.0),
            AddStudentRequest("Irene", "Semester1", 4.0),
            AddStudentRequest("Jack", "Semester2", 4.0),
            AddStudentRequest("Kate", "Semester1", 5.0),
            AddStudentRequest("Linda", "Semester1", 3.0),
            AddStudentRequest("Marc", "Semester1", 2.0),
        ).forEach { studentService.addStudent(it) }

        // When
        val result1 = studentService.getStudents("Semester1")

        // Then should receive students from the same semester and with result >= 3.0
        assertEquals(
            listOf(
                ExposedStudent("Alex"),
                ExposedStudent("Diana"),
                ExposedStudent("Fiona"),
                ExposedStudent("Helen"),
                ExposedStudent("Irene"),
                ExposedStudent("Kate"),
                ExposedStudent("Linda"),
            ), result1
        )
        assertEquals(listOf("7 students in Semester1"), logger.messages)

        // Given
        logger.cleanup()

        // When
        val result = studentService.getStudents("Semester2")

        // Then should receive students from the semester and with result >= 3.0
        assertEquals(
            listOf(
                ExposedStudent("Bob"),
                ExposedStudent("Ester"),
                ExposedStudent("Jack"),
            ), result
        )
        assertEquals(listOf("3 students in Semester2"), logger.messages)
    }
}

interface StudentRepository {
    fun addStudent(student: Student)
    fun getStudent(id: String): Student?
    fun getStudents(request: GetStudentsRequest): List<Student>
}

// Poor practice: Constructor parameters with default values would be better.
class GetStudentsRequest {
    var minResult: Double? = null
    var expectedSemester: String? = null
}

data class AddStudentRequest(
    val name: String,
    val semester: String,
    val result: Double,
)

data class Student(
    val id: String,
    val name: String,
    val creationTime: LocalDateTime,
    val semester: String,
    val result: Double,
)

data class ExposedStudent(
    val name: String,
)

interface Logger {
    fun log(message: String)
}

interface TimeProvider {
    fun now(): LocalDateTime
}

class RealTimeProvider : TimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now()
}

class FakeTimeProvider : TimeProvider {
    var now: LocalDateTime = INIT_TIME

    fun cleanup() {
        now = INIT_TIME
    }

    override fun now(): LocalDateTime = now

    companion object {
        val INIT_TIME = LocalDateTime.of(2020, 1, 1, 0, 0)
    }
}

interface UuidGenerator {
    fun generate(): String
}

class RealUuidGenerator : UuidGenerator {
    override fun generate(): String = UUID.randomUUID().toString()
}

class FakeUuidGenerator : UuidGenerator {
    var constantUuid: String? = null
    private var counter: Int = 0

    fun cleanup() {
        counter = 0
        constantUuid = null
    }

    override fun generate(): String = constantUuid
        ?: (counter++).toString()
}

class FakeLogger : Logger {
    var messages: List<String> = emptyList()

    fun cleanup() {
        messages = emptyList()
    }

    override fun log(message: String) {
        this.messages += message
    }
}

class FakeStudentRepository() : StudentRepository {
    private var students: List<Student> = emptyList()

    fun cleanup() {
        students = emptyList()
    }

    override fun addStudent(student: Student) {
        students += student
    }

    override fun getStudent(id: String): Student? = students
        .find { it.id == id }

    override fun getStudents(request: GetStudentsRequest): List<Student> =
        students
            .filter { request.expectedSemester == null || it.semester == request.expectedSemester }
            .filter { request.minResult == null || it.result >= request.minResult!! }
}
