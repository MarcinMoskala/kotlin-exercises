package domain.comment

class CommentModelFactory(
    private val uuidProvider: UuidProvider,
    private val timeProvider: TimeProvider,
) {
    fun toCommentModel(userId: String, collectionKey: String, body: AddComment) = CommentModel(
        id = uuidProvider.next(),
        collectionKey = collectionKey,
        userId = userId,
        comment = body.comment,
        date = timeProvider.now()
    )
}