package coroutines.comment.commentservice

import domain.comment.*
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
        TODO()
    }

    suspend fun getComments(
        collectionKey: String
    ): CommentsCollection = TODO()
}
