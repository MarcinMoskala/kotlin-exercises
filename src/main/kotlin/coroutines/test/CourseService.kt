package coroutines.test

import coroutines.test.articleservice.RequestAuth
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CourseService(
    private val userService: UserService,
    private val courseService: CourseProvider,
    private val courseChallengeService: CourseChallengeService,
) {
    suspend fun getUserCourse(requestAuth: RequestAuth, courseId: Long): UserCourse = coroutineScope {
        val userId = userService.readUserId(requestAuth)
        val course = async { courseService.getCourse(courseId) }
        val status = async { courseChallengeService.getUserChallengeStatus(userId) }
        val steps = course.await().steps.map { step -> mapCourseStep(step, status.await()) }
        UserCourse(
            courseId = courseId,
            name = course.await().name,
            description = course.await().description,
            steps = steps,
        )
    }

    private fun mapCourseStep(
        step: CodingChallengeConfig,
        userChallengeStatus: Map<String, ChallengeStatus>,
    ): UserCourseStep {
        return UserCourseStep(
            type = CourseStepType.CODING_CHALLENGE,
            key = step.key,
            text = step.title,
            state = when (userChallengeStatus[step.key]) {
                ChallengeStatus.INITIAL -> UserCourseStepState.INITIAL
                ChallengeStatus.SOLVED -> UserCourseStepState.SOLVED
                ChallengeStatus.IN_PROGRESS -> UserCourseStepState.IN_PROGRESS
                ChallengeStatus.IN_REVIEW -> UserCourseStepState.IN_REVIEW
                ChallengeStatus.CHANGES_REQUESTED -> UserCourseStepState.CHANGES_REQUESTED
                null -> UserCourseStepState.INITIAL
            },
        )
    }
}

data class CodingChallengeConfig(val key: String, val title: String)

enum class ChallengeStatus { INITIAL, SOLVED, IN_PROGRESS, IN_REVIEW, CHANGES_REQUESTED }

enum class CourseStepType { CODING_CHALLENGE }

enum class UserCourseStepState { INITIAL, SOLVED, IN_PROGRESS, IN_REVIEW, CHANGES_REQUESTED }

data class UserCourseStep(
    val type: CourseStepType,
    val key: String,
    val text: String,
    val state: UserCourseStepState,
)

data class CourseDetails(
    val name: String,
    val description: String,
    val steps: List<CodingChallengeConfig>,
)

data class UserCourse(
    val courseId: Long,
    val name: String,
    val description: String,
    val steps: List<UserCourseStep>,
)

interface UserService {
    fun readUserId(requestAuth: RequestAuth): Long
}

interface CourseProvider {
    suspend fun getCourse(courseId: Long): CourseDetails
}

interface CourseChallengeService {
    suspend fun getUserChallengeStatus(userId: Long): Map<String, ChallengeStatus>
}

class CourseServiceTest {

    @Test
    fun `should provide user course with challenge step status`() = runTest {
        TODO()
    }
}

