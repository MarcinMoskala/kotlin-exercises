package utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime

inline fun <reified T: Throwable> assertThrows(operation: () -> Unit) {
    val result = runCatching { operation() }
    assert(result.isFailure) { "Operation has not failed with exception" }
    val exception = result.exceptionOrNull()
    assert(exception is T) { "Incorrect exception type, it should be ${T::class}, but it is $exception" }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Flow<T>.withVirtualTime(testScope: TestScope): Flow<ValueAndTime<T>> =
    map { ValueAndTime(it, testScope.currentTime) }

data class ValueAndTime<T>(val value: T, val timeMillis: Long)
