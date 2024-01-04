package functional.collections.studentgradesintenship

import org.junit.Test
import java.util.Collections
import kotlin.random.Random
import kotlin.test.assertEquals

fun List<StudentGrades>.getBestForScholarship(
    semester: String
): List<StudentGrades> {
    val students = this
    var candidates = mutableListOf<StudentGrades>()
    for (s in students) {
        var ectsPointsGained = 0
        for (g in s.grades) {
            if (g.semester == semester && g.passing) {
                ectsPointsGained += g.ects
            }
        }
        if (ectsPointsGained > 30) {
            candidates.add(s)
        }
    }
    Collections.sort(candidates, { s1, s2 ->
        val difference =
            averageGradeFromSemester(s2, semester) -
                    averageGradeFromSemester(s1, semester)
        if (difference > 0) 1 else -1
    })
    val best = mutableListOf<StudentGrades>()
    for (i in 0 until 10) {
        val next = candidates.getOrNull(i)
        if (next != null) {
            best.add(next)
        }
    }
    return best
}

private fun averageGradeFromSemester(
    student: StudentGrades,
    semester: String
): Double {
    var sum = 0.0
    var count = 0
    for (g in student.grades) {
        if (g.semester == semester) {
            sum += g.grade
            count++
        }
    }
    return sum / count
}

data class Grade(
    val passing: Boolean,
    var ects: Int,
    var semester: String,
    var grade: Double
)

data class StudentGrades(
    val studentId: String,
    val grades: List<Grade>
)

class StudentGradesIntenshipTest {
    @Test
    fun `should return best students for scholarship`() {
        val r = Random(123456789)
        val grades = List(100) {
            StudentGrades(
                "S$it",
                List(100) {
                    Grade(
                        passing = r.nextBoolean(),
                        ects = r.nextInt(5) + 1,
                        semester = "Semester ${r.nextInt(5)}",
                        grade = r.nextDouble() * 5 + 2.0
                    )
                }
            )
        }

        val res1 = grades.getBestForScholarship("Semester 0")
        assertEquals(
            listOf("S83", "S47", "S26", "S53", "S4", "S29", "S50", "S34", "S44", "S59"),
            res1.map { it.studentId },
        )
        assertEquals(
            listOf("S30", "S66", "S49", "S14", "S6", "S12", "S25", "S99", "S7", "S40"),
            grades.getBestForScholarship("Semester 1").map { it.studentId }
        )
    }

    @Test
    fun `should return empty list if no students passed`() {
        val grades = listOf(
            StudentGrades(
                "S1",
                listOf(
                    Grade(false, 5, "Semester 0", 3.0),
                    Grade(false, 5, "Semester 1", 3.0),
                    Grade(false, 5, "Semester 2", 3.0),
                    Grade(false, 5, "Semester 3", 3.0),
                    Grade(false, 5, "Semester 4", 3.0),
                )
            ),
            StudentGrades(
                "S2",
                listOf(
                    Grade(false, 5, "Semester 0", 3.0),
                    Grade(false, 5, "Semester 1", 3.0),
                    Grade(false, 5, "Semester 2", 3.0),
                    Grade(false, 5, "Semester 3", 3.0),
                    Grade(false, 5, "Semester 4", 3.0),
                )
            ),
            StudentGrades(
                "S3",
                listOf(
                    Grade(false, 5, "Semester 0", 3.0),
                    Grade(false, 5, "Semester 1", 3.0),
                    Grade(false, 5, "Semester 2", 3.0),
                    Grade(false, 5, "Semester 3", 3.0),
                    Grade(false, 5, "Semester 4", 3.0),
                )
            ),
        )

        assertEquals(
            emptyList<String>(),
            grades.getBestForScholarship("Semester 0").map { it.studentId }
        )
    }

    @Test
    fun `should sort by average grade for semester`() {
        val grades = listOf(
            StudentGrades(
                "S1",
                listOf(
                    Grade(true, 25, "Semester 1", 3.0),
                    Grade(true, 25, "Semester 1", 3.0),
                )
            ),
            StudentGrades(
                "S2",
                listOf(
                    Grade(true, 50, "Semester 1", 4.0),
                    Grade(true, 50, "Semester 2", 1.0),
                    Grade(true, 50, "Semester 3", 1.0),
                    Grade(true, 50, "Semester 4", 1.0),
                )
            ),
            StudentGrades("S3", listOf(Grade(true, 50, "Semester 1", 5.0),)),
        )

        assertEquals(
            listOf("S3", "S2", "S1"),
            grades.getBestForScholarship("Semester 1").map { it.studentId }
        )
    }

    @Test
    fun `should return 10 best students`() {
        val grades = List(100) {
            StudentGrades(
                "S$it",
                listOf(
                    Grade(
                        passing = true,
                        ects = 50,
                        semester = "Semester 0",
                        grade = 5.0 + (0.01 * it)
                    )
                )
            )
        }

        assertEquals(
            List(10) { "S${99 - it}" },
            grades.getBestForScholarship("Semester 0").map { it.studentId }
        )
    }

    @Test
    fun `should calculate average grade from semester`() {
        assertEquals(
            3.0,
            averageGradeFromSemester(StudentGrades(
                "S1",
                listOf(
                    Grade(true, 25, "Semester 1", 3.0),
                    Grade(true, 25, "Semester 1", 3.0),
                )
            ), "Semester 1")
        )
        assertEquals(
            3.0,
            averageGradeFromSemester(StudentGrades(
                "S1",
                listOf(
                    Grade(true, 10, "Semester 1", 2.0),
                    Grade(true, 20, "Semester 1", 4.0),
                )
            ), "Semester 1")
        )
        assertEquals(
            2.0,
            averageGradeFromSemester(StudentGrades(
                "S1",
                listOf(
                    Grade(true, 10, "Semester 1", 2.0),
                    Grade(true, 20, "Semester 2", 4.0),
                )
            ), "Semester 1")
        )
    }
}
