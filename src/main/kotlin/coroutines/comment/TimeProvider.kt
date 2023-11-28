package domain.comment

import java.time.Instant

interface TimeProvider {
    fun now(): Instant
}