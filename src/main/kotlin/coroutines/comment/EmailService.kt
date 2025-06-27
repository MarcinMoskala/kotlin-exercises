package coroutines.comment

interface EmailService {
    suspend fun notifyAboutCommentInObservedCollection(email: String, collectionKey: String, comment: String?)
}