package coroutines.starting.commentservice

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommentService(
    private val commentRepository: CommentRepository,
    private val userService: UserService,
    private val commentModelFactory: CommentModelFactory,
    private val commentValidator: CommentValidator,
    private val emailService: EmailService,
    private val backgroundScope: CoroutineScope,
) {
    suspend fun addComment(
        token: String,
        collectionKey: String,
        body: AddComment
    ) {
        TODO()
    }

    suspend fun getComments(
        collectionKey: String
    ): CommentsCollection = TODO()

    // For legacy blocking calls
    fun addCommentBlocking(
        token: String,
        collectionKey: String,
        body: AddComment
    ) {
        TODO()
    }

    // For legacy blocking calls
    fun getCommentsBlocking(
        collectionKey: String
    ): CommentsCollection = TODO()
}

interface CommentRepository {
    suspend fun getComments(collectionKey: String): List<CommentModel>
    suspend fun getComment(id: String): CommentModel?
    suspend fun addComment(comment: CommentModel)
    suspend fun deleteComment(commentId: String)
    suspend fun getCollectionKeyObservers(collectionKey: String): List<String>
}

class CommentModelFactory(
    private val uuidProvider: UuidProvider,
    private val timeProvider: TimeProvider,
) {
    fun toCommentModel(userId: String, collectionKey: String, body: AddComment) = CommentModel(
        id = uuidProvider.next(),
        collectionKey = collectionKey,
        userId = userId,
        comment = body.comment,
        date = timeProvider.now()
    )
}

interface EmailService {
    suspend fun notifyAboutCommentInObservedCollection(email: String, collectionKey: String, comment: String?)
}

interface TimeProvider {
    fun now(): Instant
}

interface UuidProvider {
    fun next(): String
}

interface CommentValidator {
    @Throws(CommentValidationException::class)
    fun validate(comment: String?): Boolean
}

class CommentValidationException(message: String) : Exception(message)

data class CommentModel(
    val id: String,
    val collectionKey: String,
    val userId: String,
    val comment: String?,
    val date: Instant,
)

interface UserService {
    fun readUserId(token: String): String
    suspend fun findUser(token: String): User
    suspend fun findUserById(id: String): User
}

object NoSuchUserException: Exception("No such user")

data class CommentsCollection(
    val collectionKey: String,
    val elements: List<CommentElement>,
)

data class CommentElement(
    val id: String,
    val collectionKey: String,
    val user: User?,
    val comment: String?,
    val date: Instant,
)

data class AddComment(
    val comment: String?,
)

data class EditComment(
    val comment: String?,
)

data class User(
    val id: String,
    val email: String,
    val imageUrl: String,
    val displayName: String? = null,
    val bio: String? = null,
)

data class UserDocument(
    val _id: String,
    val email: String,
    val imageUrl: String,
    val displayName: String? = null,
    val bio: String? = null,
)

fun UserDocument.toUser() = User(
    id = _id,
    email = email,
    imageUrl = imageUrl,
    displayName = displayName,
    bio = bio
)

fun User.toUserDocument() = UserDocument(
    _id = id,
    email = email,
    imageUrl = imageUrl,
    displayName = displayName,
    bio = bio
)

class CommentServiceTests {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val commentsRepository = FakeCommentRepository()
    private val userService = FakeUserService()
    private val uuidProvider = FakeUuidProvider()
    private val timeProvider = FakeTimeProvider()
    private val commentValidator = FakeCommentValidator()
    private val emailService = FakeEmailService()
    private val commentsFactory: CommentModelFactory = CommentModelFactory(uuidProvider, timeProvider)
    private val commentService: CommentService = CommentService(
        commentsRepository,
        userService,
        commentsFactory,
        commentValidator,
        emailService,
        testScope
    )

    @Before
    fun setup() {
        userService.hasUsers(user1, user2)
        commentValidator.setShouldValidate(true)
    }

    @After
    fun cleanup() {
        timeProvider.clean()
        uuidProvider.clean()
        commentsRepository.clean()
        userService.clear()
        commentValidator.reset()
        emailService.clear()
    }

    @Test
    fun `Should add comment`() = runTest {
        // given
        commentsRepository.has(
            commentModel1,
        )
        userService.hasUsers(
            user1,
            user2
        )
        userService.hasToken(aToken, user2.id)
        uuidProvider.alwaysReturn(commentModel2.id)
        timeProvider.advanceTimeTo(commentModel2.date)

        // when
        commentService.addComment(aToken, collectionKey2, AddComment(commentModel2.comment))

        // then
        assertEquals(commentModel2, commentsRepository.getComment(commentModel2.id))
    }

    @Test
    fun `Should get comments by collection key`() = runTest {
        // given
        commentsRepository.has(
            commentModel1,
            commentModel2,
            commentDocument3
        )
        userService.hasUsers(
            user1,
            user2
        )

        // when
        val result: CommentsCollection = commentService.getComments(collectionKey1)

        // then
        with(result) {
            assertEquals(collectionKey1, collectionKey)
            assertEquals(listOf(commentElement1, commentElement3), elements)
        }
    }

    @Test
    fun `Should concurrently find users when getting comments`() = runTest {
        // given
        commentsRepository.has(
            commentModel1,
            commentModel1,
            commentModel1,
            commentModel2,
            commentDocument3,
        )
        userService.hasUsers(
            user1,
            user2
        )
        userService.findUserDelay = 1000

        // when
        commentService.getComments(collectionKey1)

        // then
        assertEquals(1000, currentTime)
    }

    @Test
    fun `Should add comment using blocking method`() {
        // given
        commentsRepository.has(
            commentModel1,
        )
        userService.hasUsers(
            user1,
            user2
        )
        userService.hasToken(aToken, user2.id)
        uuidProvider.alwaysReturn(commentModel2.id)
        timeProvider.advanceTimeTo(commentModel2.date)

        // when
        commentService.addCommentBlocking(aToken, collectionKey2, AddComment(commentModel2.comment))

        // then
        runBlocking {
            assertEquals(commentModel2, commentsRepository.getComment(commentModel2.id))
        }
    }

    @Test
    fun `Should get comments by collection key using blocking method`() {
        // given
        commentsRepository.has(
            commentModel1,
            commentModel2,
            commentDocument3
        )
        userService.hasUsers(
            user1,
            user2
        )

        // when
        val result: CommentsCollection = commentService.getCommentsBlocking(collectionKey1)

        // then
        with(result) {
            assertEquals(collectionKey1, collectionKey)
            assertEquals(listOf(commentElement1, commentElement3), elements)
        }
    }

    private val aToken = "SOME_TOKEN"
    private val collectionKey1 = "SOME_COLLECTION_KEY_1"
    private val collectionKey2 = "SOME_COLLECTION_KEY_2"
    private val date1 = Instant.parse("2018-11-30T18:35:24.00Z")
    private val date2 = Instant.parse("2019-11-30T18:35:24.00Z")
    private val userDocument1 = UserDocument(
        _id = "U_ID_1",
        email = "user1@email.com",
        imageUrl = "some_image_1",
        displayName = "some_display_name_1",
        bio = "some bio 1"
    )
    private val userDocument2 = UserDocument(
        _id = "U_ID_2",
        email = "user2@email.com",
        imageUrl = "some_image_2",
        displayName = "some_display_name_2",
        bio = "some bio 2"
    )
    private val user1 = userDocument1.toUser()
    private val user2 = userDocument2.toUser()
    private val commentModel1 = CommentModel(
        id = "C_ID_1",
        collectionKey = collectionKey1,
        userId = user1.id,
        comment = "Some comment 1",
        date = date1,
    )
    private val commentModel2 = CommentModel(
        id = "C_ID_2",
        collectionKey = collectionKey2,
        userId = user2.id,
        comment = "Some comment 2",
        date = date2,
    )
    private val commentDocument3 = CommentModel(
        id = "C_ID_3",
        collectionKey = collectionKey1,
        userId = user2.id,
        comment = "Some comment 3",
        date = date2,
    )
    private val commentElement1 = CommentElement(
        id = "C_ID_1",
        collectionKey = collectionKey1,
        user = user1,
        comment = "Some comment 1",
        date = date1,
    )
    private val commentElement2 = CommentElement(
        id = "C_ID_2",
        collectionKey = collectionKey2,
        user = user2,
        comment = "Some comment 2",
        date = date2,
    )
    private val commentElement3 = CommentElement(
        id = "C_ID_3",
        collectionKey = collectionKey1,
        user = user2,
        comment = "Some comment 3",
        date = date2,
    )

    @Test
    fun `Should add comment when validation passes`() = runTest {
        // given
        commentValidator.setShouldValidate(true)
        userService.hasToken(aToken, user2.id)
        uuidProvider.alwaysReturn(commentModel2.id)
        timeProvider.advanceTimeTo(commentModel2.date)

        // when
        commentService.addComment(aToken, collectionKey2, AddComment(commentModel2.comment))

        // then
        assertEquals(commentModel2, commentsRepository.getComment(commentModel2.id))
    }

    @Test
    fun `Should throw exception when validation fails`() = runTest {
        // given
        commentValidator.setShouldValidate(false)
        userService.hasToken(aToken, user2.id)

        // when/then
        assertFailsWith<CommentValidationException> {
            commentService.addComment(aToken, collectionKey2, AddComment(commentModel2.comment))
        }
    }

    @Test
    fun `Should throw exception when validation fails with custom predicate`() = runTest {
        // given
        commentValidator.setValidationPredicate { comment ->
            comment != null && comment.length >= 5
        }
        userService.hasToken(aToken, user2.id)

        // when/then
        assertFailsWith<CommentValidationException> {
            commentService.addComment(aToken, collectionKey2, AddComment("abc"))
        }
    }

    @Test
    fun `Should send emails to observers when comment is added`() = runTest(testDispatcher) {
        // given
        userService.hasUsers(user1, user2)
        userService.hasToken(aToken, user1.id)
        uuidProvider.alwaysReturn(commentModel1.id)
        timeProvider.advanceTimeTo(commentModel1.date)

        val observers = listOf(user2.id)
        commentsRepository.setObservers(collectionKey1, observers)

        // when
        commentService.addComment(aToken, collectionKey1, AddComment(commentModel1.comment))
        advanceUntilIdle()

        // then
        val expectedEmails = listOf(
            user2.email to "New comment in collection $collectionKey1: ${commentModel1.comment}"
        )
        assertEquals(expectedEmails, emailService.getEmailsSent())
    }

    @Test
    fun `Should send emails to multiple observers when comment is added`() = runTest(testDispatcher) {
        // given
        val user3 = User(
            id = "U_ID_3",
            email = "user3@email.com",
            imageUrl = "some_image_3",
            displayName = "some_display_name_3",
            bio = "some bio 3"
        )
        userService.hasUsers(user1, user2, user3)
        userService.hasToken(aToken, user1.id)
        uuidProvider.alwaysReturn(commentModel1.id)
        timeProvider.advanceTimeTo(commentModel1.date)

        val observers = listOf(user2.id, user3.id)
        commentsRepository.setObservers(collectionKey1, observers)

        // when
        commentService.addComment(aToken, collectionKey1, AddComment(commentModel1.comment))

        runCurrent()
        advanceUntilIdle()

        // then
        val expectedEmails = listOf(
            user2.email to "New comment in collection $collectionKey1: ${commentModel1.comment}",
            user3.email to "New comment in collection $collectionKey1: ${commentModel1.comment}"
        )
        assertEquals(expectedEmails, emailService.getEmailsSent())
    }

    @Test
    fun `Should throw exception when token is invalid`() = runTest {
        // given
        val invalidToken = "INVALID_TOKEN"

        // when/then
        assertFailsWith<NoSuchUserException> {
            commentService.addComment(invalidToken, collectionKey1, AddComment("Some comment"))
        }
    }

    @Test
    fun `Should throw exception when user ID does not exist`() = runTest {
        // given
        val nonExistentUserId = "NON_EXISTENT_USER_ID"

        val commentWithNonExistentUser = CommentModel(
            id = "C_ID_NON_EXISTENT",
            collectionKey = collectionKey1,
            userId = nonExistentUserId,
            comment = "Comment from non-existent user",
            date = date1
        )
        commentsRepository.has(commentWithNonExistentUser)

        // when/then
        assertFailsWith<NoSuchUserException> {
            commentService.getComments(collectionKey1) // This will try to find the non-existent user
        }
    }

    @Test
    fun `Should return empty list when getting comments from empty collection`() = runTest {
        // given
        val emptyCollectionKey = "EMPTY_COLLECTION_KEY"

        // when
        val result = commentService.getComments(emptyCollectionKey)

        // then
        assertEquals(emptyCollectionKey, result.collectionKey)
        assertEquals(emptyList(), result.elements)
    }

    @Test
    fun `Should not send emails when there are no observers`() = runTest {
        // given
        userService.hasToken(aToken, user1.id)
        uuidProvider.alwaysReturn(commentModel1.id)
        timeProvider.advanceTimeTo(commentModel1.date)
        commentsRepository.setObservers(collectionKey1, emptyList())

        // when
        commentService.addComment(aToken, collectionKey1, AddComment(commentModel1.comment))

        testDispatcher.scheduler.runCurrent()
        advanceUntilIdle()

        // then
        assertEquals(emptyList(), emailService.getEmailsSent())
    }

    @Test
    fun `Should handle null comment when validator allows it`() = runTest {
        // given
        userService.hasToken(aToken, user1.id)
        uuidProvider.alwaysReturn(commentModel1.id)
        timeProvider.advanceTimeTo(commentModel1.date)

        // when
        commentService.addComment(aToken, collectionKey1, AddComment(null))

        // then
        val comment = commentsRepository.getComment(commentModel1.id)
        assertEquals(null, comment?.comment)
    }

    @Test
    fun `Should throw exception when null comment is rejected by validator`() = runTest {
        // given
        userService.hasToken(aToken, user1.id)
        commentValidator.setValidationPredicate { comment -> comment != null }

        // when/then
        assertFailsWith<CommentValidationException> {
            commentService.addComment(aToken, collectionKey1, AddComment(null))
        }
    }

    @Test
    fun `Should improve concurrent test with better assertions`() = runTest {
        // given
        val comment1 = CommentModel(
            id = "C_ID_CONCURRENT_1",
            collectionKey = collectionKey1,
            userId = user1.id,
            comment = "Concurrent comment 1",
            date = date1
        )
        val comment2 = CommentModel(
            id = "C_ID_CONCURRENT_2",
            collectionKey = collectionKey1,
            userId = user1.id,
            comment = "Concurrent comment 2",
            date = date1
        )
        val comment3 = CommentModel(
            id = "C_ID_CONCURRENT_3",
            collectionKey = collectionKey1,
            userId = user1.id,
            comment = "Concurrent comment 3",
            date = date1
        )

        commentsRepository.has(comment1, comment2, comment3)
        userService.findUserDelay = 1000

        // when
        val startTime = currentTime
        val result = commentService.getComments(collectionKey1)
        val endTime = currentTime

        // then
        assertEquals(1000, endTime - startTime)

        assertEquals(collectionKey1, result.collectionKey)
        assertEquals(3, result.elements.size)

        result.elements.forEach { element ->
            assertEquals(user1, element.user)
        }

        val commentIds = result.elements.map { it.id }.toSet()
        assertEquals(setOf("C_ID_CONCURRENT_1", "C_ID_CONCURRENT_2", "C_ID_CONCURRENT_3"), commentIds)
    }

    @Test
    fun `Should call each notification in a separate coroutine`() = runTest(testDispatcher) {
        // given
        val user3 = User(
            id = "U_ID_3",
            email = "user3@email.com",
            imageUrl = "some_image_3",
            displayName = "some_display_name_3",
            bio = "some bio 3"
        )
        val user4 = User(
            id = "U_ID_4",
            email = "user4@email.com",
            imageUrl = "some_image_4",
            displayName = "some_display_name_4",
            bio = "some bio 4"
        )
        userService.hasUsers(user1, user2, user3, user4)
        userService.hasToken(aToken, user1.id)
        uuidProvider.alwaysReturn(commentModel1.id)
        timeProvider.advanceTimeTo(commentModel1.date)

         val observers = listOf(user2.id, user3.id, user4.id)
        commentsRepository.setObservers(collectionKey1, observers)

        emailService.notificationDelay = 1000

        // when
        commentService.addComment(aToken, collectionKey1, AddComment(commentModel1.comment))

        runCurrent()
        advanceUntilIdle()

        // then
        val expectedEmails = listOf(
            user2.email to "New comment in collection $collectionKey1: ${commentModel1.comment}",
            user3.email to "New comment in collection $collectionKey1: ${commentModel1.comment}",
            user4.email to "New comment in collection $collectionKey1: ${commentModel1.comment}"
        )
        assertEquals(expectedEmails, emailService.getEmailsSent())

        assertEquals(3, emailService.getMaxConcurrentNotifications())
    }

    @Test
    fun `Should not wait for notification process to complete`() = runTest(testDispatcher) {
        // given
        userService.hasUsers(user1, user2)
        userService.hasToken(aToken, user1.id)
        uuidProvider.alwaysReturn(commentModel1.id)
        timeProvider.advanceTimeTo(commentModel1.date)

        val observers = listOf(user2.id)
        commentsRepository.setObservers(collectionKey1, observers)

        emailService.notificationDelay = 10000

        // when
        val startTime = currentTime
        commentService.addComment(aToken, collectionKey1, AddComment(commentModel1.comment))
        val endTime = currentTime

        // then
        assertEquals(0, endTime - startTime)

        runCurrent()
        assertTrue(emailService.isNotificationStarted())
        assertFalse(emailService.isNotificationCompleted())
        assertEquals(emptyList(), emailService.getEmailsSent())

        advanceUntilIdle()

        assertTrue(emailService.isNotificationCompleted())
        val expectedEmails = listOf(
            user2.email to "New comment in collection $collectionKey1: ${commentModel1.comment}"
        )
        assertEquals(expectedEmails, emailService.getEmailsSent())
    }

    @Test
    fun `Should not fetch the same user more than once when getting comments`() = runTest {
        // given
        val comment1 = CommentModel(
            id = "C_ID_DEDUP_1",
            collectionKey = collectionKey1,
            userId = user1.id,
            comment = "Comment 1 from user1",
            date = date1
        )
        val comment2 = CommentModel(
            id = "C_ID_DEDUP_2",
            collectionKey = collectionKey1,
            userId = user1.id,  // Same user as comment1
            comment = "Comment 2 from user1",
            date = date1
        )
        val comment3 = CommentModel(
            id = "C_ID_DEDUP_3",
            collectionKey = collectionKey1,
            userId = user2.id,
            comment = "Comment from user2",
            date = date1
        )
        val comment4 = CommentModel(
            id = "C_ID_DEDUP_4",
            collectionKey = collectionKey1,
            userId = user2.id, 
            comment = "Another comment from user2",
            date = date1
        )

        commentsRepository.has(comment1, comment2, comment3, comment4)
        userService.hasUsers(user1, user2)

        // when
        val result = commentService.getComments(collectionKey1)

        // then
        assertEquals(4, result.elements.size)

        assertEquals(1, userService.getFindUserByIdCalls(user1.id))
        assertEquals(1, userService.getFindUserByIdCalls(user2.id))

        val allCalls = userService.getAllFindUserByIdCalls()
        assertEquals(2, allCalls.size)
        assertEquals(2, allCalls.values.sum())
    }

    class FakeTimeProvider : TimeProvider {
        private var currentTime = DEFAULT_START

        override fun now(): Instant = currentTime

        fun advanceTimeTo(instant: Instant) {
            currentTime = instant
        }

        fun advanceTimeByDays(days: Int) {
            currentTime = currentTime.plusSeconds(1L * days * 60 * 60 * 24)
        }

        fun clean() {
            currentTime = DEFAULT_START
        }

        fun advanceTime() {
            currentTime = currentTime.plusSeconds(10)
        }

        companion object {
            val DEFAULT_START = Instant.parse("2018-11-30T18:35:24.00Z")
        }
    }

    class FakeCommentRepository: CommentRepository {
        private var comments = listOf<CommentModel>()
        private var collectionObservers = mapOf<String, List<String>>()

        fun has(vararg comment: CommentModel) {
            comments = comments + comment
        }

        fun clean() {
            comments = emptyList()
            collectionObservers = emptyMap()
        }

        fun setObservers(collectionKey: String, observers: List<String>) {
            collectionObservers = collectionObservers + (collectionKey to observers)
        }

        override suspend fun getComments(collectionKey: String): List<CommentModel> =
            comments.filter { it.collectionKey == collectionKey }

        override suspend fun getComment(id: String): CommentModel? =
            comments.find { it.id == id }

        override suspend fun addComment(comment: CommentModel) {
            comments = comments + comment
        }

        override suspend fun deleteComment(commentId: String) {
            TODO("Not yet implemented")
        }

        override suspend fun getCollectionKeyObservers(collectionKey: String): List<String> =
            collectionObservers[collectionKey] ?: emptyList()
    }

    class FakeCommentValidator : CommentValidator {
        private var shouldValidate = true
        private var validationPredicate: (String?) -> Boolean = { true }

        fun setShouldValidate(shouldValidate: Boolean) {
            this.shouldValidate = shouldValidate
        }

        fun setValidationPredicate(predicate: (String?) -> Boolean) {
            this.validationPredicate = predicate
        }

        fun reset() {
            shouldValidate = true
            validationPredicate = { true }
        }

        override fun validate(comment: String?): Boolean {
            return shouldValidate && validationPredicate(comment)
        }
    }

    class FakeEmailService : EmailService {
        private var emailsSent = mutableListOf<Pair<String, String>>()
        var notificationDelay: Long = 1000 
        private val concurrentNotifications = AtomicInteger(0)
        private var maxConcurrentNotifications = 0
        private val notificationStarted = AtomicBoolean(false)
        private val notificationCompleted = AtomicBoolean(false)

        override suspend fun notifyAboutCommentInObservedCollection(email: String, collectionKey: String, comment: String?) {
            notificationStarted.set(true)
            val currentConcurrent = concurrentNotifications.incrementAndGet()

            synchronized(this) {
                if (currentConcurrent > maxConcurrentNotifications) {
                    maxConcurrentNotifications = currentConcurrent
                }
            }

            delay(notificationDelay)

            val body = "New comment in collection $collectionKey: $comment"

            synchronized(this) {
                emailsSent.add(email to body)
            }

            concurrentNotifications.decrementAndGet()
            notificationCompleted.set(true)
        }

        fun getEmailsSent(): List<Pair<String, String>> = emailsSent.toList()

        fun getMaxConcurrentNotifications(): Int = maxConcurrentNotifications

        fun isNotificationStarted(): Boolean = notificationStarted.get()

        fun isNotificationCompleted(): Boolean = notificationCompleted.get()

        fun clear() {
            synchronized(this) {
                emailsSent.clear()
            }
            maxConcurrentNotifications = 0
            concurrentNotifications.set(0)
            notificationStarted.set(false)
            notificationCompleted.set(false)
        }
    }

    class FakeUserService : UserService {
        var findUserDelay: Long? = null
        private var users = listOf<User>()
        private var tokens = mapOf<String, String>()
        private val findUserByIdCalls = mutableMapOf<String, Int>()

        fun hasUsers(vararg user: User) {
            users = users + user
        }

        fun hasToken(token: String, userId: String) {
            tokens = tokens + (token to userId)
        }

        fun clear() {
            users = emptyList()
            tokens = mapOf()
            findUserDelay = null
            findUserByIdCalls.clear()
        }

        fun getFindUserByIdCalls(id: String): Int {
            return findUserByIdCalls[id] ?: 0
        }

        fun getAllFindUserByIdCalls(): Map<String, Int> {
            return findUserByIdCalls.toMap()
        }

        override fun readUserId(token: String): String =
            tokens[token] ?: throw NoSuchUserException

        override suspend fun findUser(token: String): User {
            findUserDelay?.let { delay(it) }
            return findUserById(readUserId(token))
        }

        override suspend fun findUserById(id: String): User {
            findUserDelay?.let { delay(it) }
            findUserByIdCalls[id] = (findUserByIdCalls[id] ?: 0) + 1
            return users.find { it.id == id } ?: throw NoSuchUserException
        }
    }

    class FakeUuidProvider: UuidProvider {
        private var counter = 1
        private var constantReturn: String? = null

        override fun next(): String = constantReturn ?: "UUID#" + (counter++)

        fun clean() {
            counter = 1
            constantReturn = null
        }

        fun alwaysReturn(value: String) {
            constantReturn = value
        }
    }
}
