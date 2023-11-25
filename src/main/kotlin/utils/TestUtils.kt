package utils

inline fun <reified T: Throwable> assertThrows(operation: () -> Unit) {
    val result = runCatching { operation() }
    assert(result.isFailure) { "Operation has not failed with exception" }
    val exception = result.exceptionOrNull()
    assert(exception is T) { "Incorrect exception type, it should be ${T::class}, but it is $exception" }
}
