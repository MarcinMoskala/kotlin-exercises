@file:OptIn(ExperimentalCoroutinesApi::class)

package domain.comment

import comment.FakeCommentsRepository
import comment.FakeUserService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CommentServiceTests {
    private val commentsRepository = FakeCommentsRepository()
    private val userService = FakeUserService()
    private val uuidProvider = FakeUuidProvider()
    private val timeProvider = FakeTimeProvider()
    private val commentsFactory: CommentFactory = CommentFactory(uuidProvider, timeProvider)
    private val commentsService: CommentsService = CommentsService(commentsRepository, userService, commentsFactory)

    @Before
    fun setup() {

    }

    @After
    fun cleanup() {
        timeProvider.clean()
        uuidProvider.clean()
        commentsRepository.clean()
        userService.clear()
    }

    @Test
    fun `Should add comment`() = runTest {
        // given
        commentsRepository.has(
            commentDocument1,
        )
        userService.hasUsers(
            user1,
            user2
        )
        userService.hasToken(aToken, user2.id)
        uuidProvider.alwaysReturn(commentDocument2._id)
        timeProvider.advanceTimeTo(commentDocument2.date)

        // when
        commentsService.addComment(aToken, collectionKey2, AddComment(commentDocument2.comment))

        // then
        assertEquals(commentDocument2, commentsRepository.getComment(commentDocument2._id))
    }

    @Test
    fun `Should get comments by collection key`() = runTest {
        // given
        commentsRepository.has(
            commentDocument1,
            commentDocument2,
            commentDocument3
        )
        userService.hasUsers(
            user1,
            user2
        )

        // when
        val result: CommentsCollection = commentsService.getComments(collectionKey1)

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
            commentDocument1,
            commentDocument1,
            commentDocument1,
            commentDocument2,
            commentDocument3,
        )
        userService.hasUsers(
            user1,
            user2
        )
        userService.findUserDelay = 1000

        // when
        commentsService.getComments(collectionKey1)

        // then
        assertEquals(1000, currentTime)
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
    private val commentDocument1 = CommentDocument(
        _id = "C_ID_1",
        collectionKey = collectionKey1,
        userId = user1.id,
        comment = "Some comment 1",
        date = date1,
    )
    private val commentDocument2 = CommentDocument(
        _id = "C_ID_2",
        collectionKey = collectionKey2,
        userId = user2.id,
        comment = "Some comment 2",
        date = date2,
    )
    private val commentDocument3 = CommentDocument(
        _id = "C_ID_3",
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
}