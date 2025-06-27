package coroutines.comment.commentservice

import domain.comment.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

class CommentService(
    private val commentRepository: CommentRepository,
    private val userService: UserService,
    private val commentModelFactory: CommentModelFactory,
    private val commentValidator: CommentValidator,
) {
    suspend fun addComment(
        token: String,
        collectionKey: String,
        body: AddComment
    ) {
        val userId = userService.readUserId(token)

        if (!commentValidator.validate(body.comment)) {
            throw CommentValidationException("Invalid comment")
        }

        val commentModel = commentModelFactory
            .toCommentModel(userId, collectionKey, body)
        commentRepository.addComment(commentModel)
    }

    suspend fun getComments(
        collectionKey: String
    ) = coroutineScope {
        val commentDocuments = commentRepository
            .getComments(collectionKey)
        CommentsCollection(
            collectionKey = collectionKey,
            elements = commentDocuments
                .map { async { makeCommentElement(it) } }
                .awaitAll()
        )
    }

    // For legacy blocking calls
    fun addCommentBlocking(
        token: String,
        collectionKey: String,
        body: AddComment
    ) = runBlocking {
        addComment(token, collectionKey, body)
    }

    // For legacy blocking calls
    fun getCommentsBlocking(
        collectionKey: String
    ): CommentsCollection = runBlocking {
        getComments(collectionKey)
    }

    private suspend fun makeCommentElement(
        commentModel: CommentModel
    ) = CommentElement(
        id = commentModel.id,
        collectionKey = commentModel.collectionKey,
        user = userService.findUserById(commentModel.userId),
        comment = commentModel.comment,
        date = commentModel.date,
    )
}
