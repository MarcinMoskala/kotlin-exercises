package domain.comment

class CommentFactory(
    private val uuidProvider: UuidProvider,
    private val timeProvider: TimeProvider,
) {
    fun toCommentDocument(userId: String, collectionKey: String, body: AddComment) = CommentDocument(
        _id = uuidProvider.next(),
        collectionKey = collectionKey,
        userId = userId,
        comment = body.comment,
        date = timeProvider.now()
    )
}