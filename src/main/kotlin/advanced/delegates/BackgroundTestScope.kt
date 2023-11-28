@file:OptIn(ExperimentalCoroutinesApi::class)

package advanced.delegates

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlin.coroutines.ContinuationInterceptor

class BackgroundTestScope(
    private val scope: CoroutineScope,
) : CoroutineScope by scope {
}

fun main() {
    TestScope()
}
