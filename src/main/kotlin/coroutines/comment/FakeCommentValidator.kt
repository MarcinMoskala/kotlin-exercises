package comment

import coroutines.comment.CommentValidator

/**
 * Fake implementation of CommentValidator for testing.
 */
class FakeCommentValidator : CommentValidator {
    private var shouldValidate = true
    private var validationPredicate: (String?) -> Boolean = { true }

    /**
     * Set whether validation should pass or fail.
     * 
     * @param shouldValidate If true, validate will return true, otherwise false
     */
    fun setShouldValidate(shouldValidate: Boolean) {
        this.shouldValidate = shouldValidate
    }

    /**
     * Set a custom validation predicate.
     * 
     * @param predicate A function that takes a comment and returns whether it's valid
     */
    fun setValidationPredicate(predicate: (String?) -> Boolean) {
        this.validationPredicate = predicate
    }

    /**
     * Reset the validator to its default state (always validates).
     */
    fun reset() {
        shouldValidate = true
        validationPredicate = { true }
    }

    override fun validate(comment: String?): Boolean {
        return shouldValidate && validationPredicate(comment)
    }
}