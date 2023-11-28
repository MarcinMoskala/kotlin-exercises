package domain.comment

import kotlinx.coroutines.coroutineScope

class CommentsService(
    private val commentsRepository: CommentsRepository,
    private val userService: UserService,
    private val commentFactory: CommentFactory
) {
    // TODO: Should read user id from token, transform body to comment document, and add it to comments repository
    suspend fun addComment(token: String, collectionKey: String, body: AddComment) {
        TODO()
    }

    // TODO: Should get comments with users and return them as a collection
    suspend fun getComments(collectionKey: String): CommentsCollection {
        TODO()
    }

    suspend fun deleteComment(token: String, commentId: String) = coroutineScope {
        val userId = userService.readUserId(token)

        val comment = commentsRepository.getComment(commentId)
        requireNotNull(comment) { "Comment does not exist" }
        require(comment.userId == userId) { "Not an owner" }

        commentsRepository.deleteComment(commentId)
    }

    private suspend fun makeCommentsCollection(
        commentDocuments: List<CommentDocument>,
        collectionKey: String
    ): CommentsCollection = coroutineScope {
        CommentsCollection(
            collectionKey = collectionKey,
            elements = makeCommentsElements(commentDocuments)
        )
    }

    // TODO: Should concurrently transform comment documents to comment elements
    private suspend fun makeCommentsElements(commentDocuments: List<CommentDocument>) =
        commentDocuments
            .map { makeCommentElement(it) }

    private suspend fun makeCommentElement(commentDocument: CommentDocument) = CommentElement(
        id = commentDocument._id,
        collectionKey = commentDocument.collectionKey,
        user = userService.findUserById(commentDocument.userId),
        comment = commentDocument.comment,
        date = commentDocument.date,
    )
}