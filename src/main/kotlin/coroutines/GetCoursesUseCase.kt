@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package coroutines

import coroutines.examples.sus.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.seconds

class GetCoursesUseCase(
    private val coursesNetworkRepository: CoursesNetworkRepository,
) {
    private var lastCourses: Courses? = null

    fun getCourses(): Flow<Courses> =
        coursesNetworkRepository.getCourses()
            .onEach { lastCourses = it }
            .onStart { lastCourses?.let { emit(it) } }
}

val viewModelScope = CoroutineScope(SupervisorJob())

data class Courses(val courses: List<Course>)
data class Course(val courseId: String)

interface CoursesDatabaseRepository {
    fun getCourses(): Flow<Courses>
    suspend fun saveCourses(courses: Courses)
}

interface CoursesNetworkRepository {
    fun getCourses(): Flow<Courses>
}
