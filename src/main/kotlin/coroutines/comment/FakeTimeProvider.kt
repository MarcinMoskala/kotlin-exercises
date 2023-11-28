package domain.comment

import java.time.Instant

class FakeTimeProvider : TimeProvider {
    private var currentTime = DEFAULT_START

    override fun now(): Instant = currentTime

    fun advanceTimeTo(instant: Instant) {
        currentTime = instant
    }

    fun advanceTimeByDays(days: Int) {
        currentTime = currentTime.plusSeconds(1L * days * 60 * 60 * 24)
    }

    fun clean() {
        currentTime = DEFAULT_START
    }

    fun advanceTime() {
        currentTime = currentTime.plusSeconds(10)
    }

    companion object {
        val DEFAULT_START = Instant.parse("2018-11-30T18:35:24.00Z")
    }
}