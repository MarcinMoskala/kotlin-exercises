package domain.comment

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class CommentService(
    private val commentRepository: CommentRepository,
    private val userService: UserService,
    private val commentFactory: CommentFactory
) {
    suspend fun addComment(
        token: String,
        collectionKey: String,
        body: AddComment
    ) {
        val userId = userService.readUserId(token)
        val commentDocument = commentFactory
            .toCommentDocument(userId, collectionKey, body)
        commentRepository.addComment(commentDocument)
    }

    suspend fun getComments(
        collectionKey: String
    ) = coroutineScope {
        val commentDocuments = commentRepository
            .getComments(collectionKey)
        val users: Map<String, User> = commentDocuments
            .map { it.userId }
            .toSet()
            .map { async { userService.findUserById(it) } }
            .awaitAll()
            .associateBy { it.id }

        CommentsCollection(
            collectionKey = collectionKey,
            elements = commentDocuments.map {
                val user = users[it.userId]
                makeCommentElement(it, user)
            }
        )
    }

    private fun makeCommentElement(
        commentDocument: CommentDocument,
        user: User?,
    ) = CommentElement(
        id = commentDocument._id,
        collectionKey = commentDocument.collectionKey,
        user = user,
        comment = commentDocument.comment,
        date = commentDocument.date,
    )
}
