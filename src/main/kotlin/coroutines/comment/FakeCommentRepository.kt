package comment

import coroutines.comment.CommentModel
import coroutines.comment.CommentRepository

class FakeCommentRepository: CommentRepository {
    private var comments = listOf<CommentModel>()
    private var collectionObservers = mapOf<String, List<String>>()

    fun has(vararg comment: CommentModel) {
        comments = comments + comment
    }

    fun clean() {
        comments = emptyList()
        collectionObservers = emptyMap()
    }

    fun setObservers(collectionKey: String, observers: List<String>) {
        collectionObservers = collectionObservers + (collectionKey to observers)
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

    override suspend fun getCollectionKeyObservers(collectionKey: String): List<String> =
        collectionObservers[collectionKey] ?: emptyList()
}
