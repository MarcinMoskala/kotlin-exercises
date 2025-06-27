package domain.comment

/**
 * Interface for validating comments before they are added to the repository.
 */
interface CommentValidator {
    /**
     * Validates a comment.
     * 
     * @param comment The comment to validate
     * @return true if the comment is valid, false otherwise
     */
    fun validate(comment: String?): Boolean
}

/**
 * Exception thrown when comment validation fails.
 */
class CommentValidationException(message: String) : Exception(message)