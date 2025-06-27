package coroutines.comment

import comment.FakeCommentRepository
import comment.FakeCommentValidator
import comment.FakeEmailService
import comment.FakeUserService
import coroutines.comment.commentservice.CommentService
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.assertFalse

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
        // Initialize common test data
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

    // Fake Data
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

        // Set up observers for the collection
        val observers = listOf(user2.id)
        commentsRepository.setObservers(collectionKey1, observers)

        // when
        commentService.addComment(aToken, collectionKey1, AddComment(commentModel1.comment))

        // Use the testDispatcher to run all pending tasks
        runCurrent()
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

        // Set up multiple observers for the collection
        val observers = listOf(user2.id, user3.id)
        commentsRepository.setObservers(collectionKey1, observers)

        // when
        commentService.addComment(aToken, collectionKey1, AddComment(commentModel1.comment))

        // Use the testDispatcher to run all pending tasks
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
        // Not registering this token with hasToken, so it will be invalid

        // when/then
        assertFailsWith<NoSuchUserException> {
            commentService.addComment(invalidToken, collectionKey1, AddComment("Some comment"))
        }
    }

    @Test
    fun `Should throw exception when user ID does not exist`() = runTest {
        // given
        val nonExistentUserId = "NON_EXISTENT_USER_ID"

        // Add a comment with a non-existent user ID
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

        // No observers set up for the collection
        commentsRepository.setObservers(collectionKey1, emptyList())

        // when
        commentService.addComment(aToken, collectionKey1, AddComment(commentModel1.comment))

        // Use the testDispatcher to run all pending tasks
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
        // Default validator allows null comments

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
        // Configure validator to reject null comments
        commentValidator.setValidationPredicate { comment -> comment != null }

        // when/then
        assertFailsWith<CommentValidationException> {
            commentService.addComment(aToken, collectionKey1, AddComment(null))
        }
    }

    @Test
    fun `Should improve concurrent test with better assertions`() = runTest {
        // given
        // Create three different comments with the same user ID but different comment IDs
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
        // Verify that the operation took exactly the time of one user lookup, not three
        assertEquals(1000, endTime - startTime)

        // Verify the result contains the expected number of comments
        assertEquals(collectionKey1, result.collectionKey)
        assertEquals(3, result.elements.size)

        // Verify all comments have the same user
        result.elements.forEach { element ->
            assertEquals(user1, element.user)
        }

        // Verify the comments have the expected IDs
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

        // Set up multiple observers for the collection
        val observers = listOf(user2.id, user3.id, user4.id)
        commentsRepository.setObservers(collectionKey1, observers)

        // Set a delay for notifications to ensure we can detect concurrency
        emailService.notificationDelay = 1000

        // when
        commentService.addComment(aToken, collectionKey1, AddComment(commentModel1.comment))

        // Use the testDispatcher to run all pending tasks
        runCurrent()
        advanceUntilIdle()

        // then
        // Verify all emails were sent
        val expectedEmails = listOf(
            user2.email to "New comment in collection $collectionKey1: ${commentModel1.comment}",
            user3.email to "New comment in collection $collectionKey1: ${commentModel1.comment}",
            user4.email to "New comment in collection $collectionKey1: ${commentModel1.comment}"
        )
        assertEquals(expectedEmails, emailService.getEmailsSent())

        // Verify that notifications were called concurrently
        // If they were called sequentially, maxConcurrentNotifications would be 1
        // If they were called concurrently, maxConcurrentNotifications should be equal to the number of observers
        assertEquals(3, emailService.getMaxConcurrentNotifications())
    }

    @Test
    fun `Should not wait for notification process to complete`() = runTest(testDispatcher) {
        // given
        userService.hasUsers(user1, user2)
        userService.hasToken(aToken, user1.id)
        uuidProvider.alwaysReturn(commentModel1.id)
        timeProvider.advanceTimeTo(commentModel1.date)

        // Set up observers for the collection
        val observers = listOf(user2.id)
        commentsRepository.setObservers(collectionKey1, observers)

        // Set a long delay for notifications to ensure we can detect if we're waiting
        emailService.notificationDelay = 10000

        // when
        val startTime = currentTime
        commentService.addComment(aToken, collectionKey1, AddComment(commentModel1.comment))
        val endTime = currentTime

        // then
        // Verify that addComment returns immediately, without waiting for the notification process to complete
        assertEquals(0, endTime - startTime)

        // Verify that the notification process is started but not completed
        runCurrent() // Run the initial part of the background task
        assertTrue(emailService.isNotificationStarted())
        assertFalse(emailService.isNotificationCompleted())
        assertEquals(emptyList(), emailService.getEmailsSent())

        // Advance time to complete the notification process
        advanceUntilIdle()

        // Verify that the notification process is completed and emails are sent
        assertTrue(emailService.isNotificationCompleted())
        val expectedEmails = listOf(
            user2.email to "New comment in collection $collectionKey1: ${commentModel1.comment}"
        )
        assertEquals(expectedEmails, emailService.getEmailsSent())
    }
}
