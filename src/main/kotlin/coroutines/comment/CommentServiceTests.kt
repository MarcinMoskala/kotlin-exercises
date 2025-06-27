package domain.comment

import comment.FakeCommentRepository
import comment.FakeCommentValidator
import comment.FakeUserService
import coroutines.comment.commentservice.CommentService
import domain.comment.CommentValidationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CommentServiceTests {
    private val commentsRepository = FakeCommentRepository()
    private val userService = FakeUserService()
    private val uuidProvider = FakeUuidProvider()
    private val timeProvider = FakeTimeProvider()
    private val commentValidator = FakeCommentValidator()
    private val commentsFactory: CommentModelFactory = CommentModelFactory(uuidProvider, timeProvider)
    private val commentService: CommentService = CommentService(commentsRepository, userService, commentsFactory, commentValidator)

    @Before
    fun setup() {

    }

    @After
    fun cleanup() {
        timeProvider.clean()
        uuidProvider.clean()
        commentsRepository.clean()
        userService.clear()
        commentValidator.reset()
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
}
