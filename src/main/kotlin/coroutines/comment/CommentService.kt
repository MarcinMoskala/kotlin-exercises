package coroutines.comment.commentservice

import coroutines.comment.*
import kotlinx.coroutines.*

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
        val userId = userService.readUserId(token)

        if (!commentValidator.validate(body.comment)) {
            throw CommentValidationException("Invalid comment")
        }

        val commentModel = commentModelFactory
            .toCommentModel(userId, collectionKey, body)
        commentRepository.addComment(commentModel)

        notifyAddCommentObservers(collectionKey, body)
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

    private fun notifyAddCommentObservers(collectionKey: String, body: AddComment) {
        backgroundScope.launch {
            val observerIds = commentRepository.getCollectionKeyObservers(collectionKey)
            val users = observerIds.map { userService.findUserById(it) }

            users.forEach { user ->
                launch {
                    emailService.notifyAboutCommentInObservedCollection(
                        email = user.email,
                        collectionKey = collectionKey,
                        comment = body.comment
                    )
                }
            }
        }
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
