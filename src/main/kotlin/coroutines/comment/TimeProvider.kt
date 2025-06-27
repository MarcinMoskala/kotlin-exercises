package coroutines.comment

import java.time.Instant

interface TimeProvider {
    fun now(): Instant
}