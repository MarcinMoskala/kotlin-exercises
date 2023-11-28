package domain.comment

import java.time.Instant

interface CommentsRepository {
    suspend fun getComments(collectionKey: String): List<CommentDocument>
    suspend fun getComment(id: String): CommentDocument?
    suspend fun addComment(comment: CommentDocument)
    suspend fun deleteComment(commentId: String)
}

data class CommentDocument(
    val _id: String,
    val collectionKey: String,
    val userId: String,
    val comment: String?,
    val date: Instant,
)

interface UserService {
    fun readUserId(token: String): String
    suspend fun findUser(token: String): User
    suspend fun findUserById(id: String): User
}

object NoSuchUserException: Exception("No such user")

data class CommentsCollection(
    val collectionKey: String,
    val elements: List<CommentElement>,
)

data class CommentElement(
    val id: String,
    val collectionKey: String,
    val user: User?,
    val comment: String?,
    val date: Instant,
)

data class AddComment(
    val comment: String?,
)

data class EditComment(
    val comment: String?,
)

data class User(
    val id: String,
    val email: String,
    val imageUrl: String,
    val displayName: String? = null,
    val bio: String? = null,
)

data class UserDocument(
    val _id: String,
    val email: String,
    val imageUrl: String,
    val displayName: String? = null,
    val bio: String? = null,
)

fun UserDocument.toUser() = User(
    id = _id,
    email = email,
    imageUrl = imageUrl,
    displayName = displayName,
    bio = bio
)

fun User.toUserDocument() = UserDocument(
    _id = id,
    email = email,
    imageUrl = imageUrl,
    displayName = displayName,
    bio = bio
)