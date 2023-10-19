package functional.project

import java.time.LocalDateTime

interface TimeProvider {
    fun now(): LocalDateTime
}

class RealTimeProvider : TimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now()
}

class FakeTimeProvider : TimeProvider {
    var now: LocalDateTime = INIT_TIME

    fun cleanup() {
        now = INIT_TIME
    }

    override fun now(): LocalDateTime = now

    companion object {
        val INIT_TIME = LocalDateTime.of(2020, 1, 1, 0, 0)
    }
}
