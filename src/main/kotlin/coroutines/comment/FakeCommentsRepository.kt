package comment

import domain.comment.CommentDocument
import domain.comment.CommentsRepository

class FakeCommentsRepository: CommentsRepository {
    private var comments = listOf<CommentDocument>()

    fun has(vararg comment: CommentDocument) {
        comments = comments + comment
    }

    fun clean() {
        comments = emptyList()
    }

    override suspend fun getComments(collectionKey: String): List<CommentDocument> =
        comments.filter { it.collectionKey == collectionKey }

    override suspend fun getComment(id: String): CommentDocument? =
        comments.find { it._id == id }

    override suspend fun addComment(comment: CommentDocument) {
        comments = comments + comment
    }

    override suspend fun deleteComment(commentId: String) {
        TODO("Not yet implemented")
    }
}