package comment

import domain.comment.CommentModel
import domain.comment.CommentRepository

class FakeCommentRepository: CommentRepository {
    private var comments = listOf<CommentModel>()

    fun has(vararg comment: CommentModel) {
        comments = comments + comment
    }

    fun clean() {
        comments = emptyList()
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
}
