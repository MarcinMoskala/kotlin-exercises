package functional.collections.passingstudents

import org.junit.Test
import kotlin.test.assertEquals

// Implement makePassingStudentsListText method to display a list of students
// who have got more than 15 points in the semester and a result of at least 50.
// Display in alphabetical order (surname then name), in the format:
// “{name} {surname}, {result}”
fun List<Student>.makePassingStudentList(): String = TODO()

data class Student(
    val name: String,
    val surname: String,
    val result: Double,
    val pointsInSemester: Int
)

class PassingStudentsListTest {
    val internshipStudent = Student("Marc", "Smith", 87.0, 32)
    val studentWithTooLowResultToInternship = Student("Marcus", "Smith", 37.0, 32)
    val studentWithNotEnoughPointsForInternship = Student("Marcello", "Smith", 87.0, 12)
    val studentNotPassingBecauseOfResult = Student("Peter", "Jackson", 21.0, 24)
    val studentNotPassingBecauseOfPoints = Student("Michael", "Angelo", 71.0, 12)

    val allStudents = listOf(
            internshipStudent,
            studentWithTooLowResultToInternship,
            studentWithNotEnoughPointsForInternship,
            studentNotPassingBecauseOfResult,
            Student("Noely", "Peterson", 91.0, 22),
            studentNotPassingBecauseOfPoints,
            Student("Noe", "Samson", 41.0, 18),
            Student("Timothy", "Johnson", 51.0, 15),
            Student("Noe", "Peterson", 91.0, 22),
            Student("Ester", "Adams", 81.0, 30),
            Student("Dior", "Angel", 88.5, 38),
            Student("Naja", "Marcson", 100.0, 31),
            Student("Oregon", "Dart", 85.5, 30),
            Student("Ron", "Peters", 89.0, 31),
            Student("Harry", "Potter", 80.0, 30),
            Student("Sansa", "Stark", 49.5, 14),
            Student("Jamme", "Lannister", 80.0, 30),
            Student("Alex", "Nolan", 86.0, 33),
            Student("Jon", "Johnson", 85.1, 31),
            Student("James", "Johnson", 85.2, 31),
            Student("Jack", "Johnson", 85.3, 31)
    )

    @Test
    fun `Single student that matches criteria is displayed`() {
        val text = listOf(internshipStudent).makePassingStudentList()
        val expected = "Marc Smith, 87.0"
        assertEquals(expected, text)
    }

    @Test
    fun `Single student with too low result doesn't get internship`() {
        val text = listOf(studentNotPassingBecauseOfResult).makePassingStudentList()
        assertEquals("", text)
    }

    @Test
    fun `15 points is not acceptable`() {
        val student = Student("Noely", "Peterson", 81.0, 15)
        val text = listOf(student).makePassingStudentList()
        assertEquals("", text)
    }

    @Test
    fun `result 50 points is acceptable`() {
        val student = Student("Noely", "Peterson", 50.0, 25)
        val text = listOf(student).makePassingStudentList()
        assertEquals("Noely Peterson, 50.0", text)
    }

    @Test
    fun `Students are displayed in an alphanumerical order sorted by surname and then by name`() {
        val students = listOf(
            Student(name = "B", surname = "A", result = 81.0, pointsInSemester = 16),
            Student(name = "B", surname = "B", result = 82.0, pointsInSemester = 16),
            Student(name = "A", surname = "A", result = 83.0, pointsInSemester = 16),
            Student(name = "A", surname = "B", result = 84.0, pointsInSemester = 16)
        )

        // When
        val text = students.makePassingStudentList()

        // Then
        val expected = """
            A A, 83.0
            B A, 81.0
            A B, 84.0
            B B, 82.0
        """.trimIndent()
        assertEquals(expected, text)
    }

    @Test
    fun `Single student with not enough doesn't get internship`() {
        val text = listOf(studentNotPassingBecauseOfPoints).makePassingStudentList()
        assertEquals("", text)
    }

    @Test
    fun `Complex test`() {
        val text = allStudents.makePassingStudentList()
        val expected = """
            Ester Adams, 81.0
            Dior Angel, 88.5
            Oregon Dart, 85.5
            Jack Johnson, 85.3
            James Johnson, 85.2
            Jon Johnson, 85.1
            Jamme Lannister, 80.0
            Naja Marcson, 100.0
            Alex Nolan, 86.0
            Ron Peters, 89.0
            Noe Peterson, 91.0
            Noely Peterson, 91.0
            Harry Potter, 80.0
            Marc Smith, 87.0
        """.trimIndent()
        assertEquals(expected, text)
    }

}
