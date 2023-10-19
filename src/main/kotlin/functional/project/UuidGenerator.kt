package functional.project

import java.util.*

interface UuidGenerator {
    fun generate(): String
}

class RealUuidGenerator : UuidGenerator {
    override fun generate(): String = UUID.randomUUID().toString()
}

class FakeUuidGenerator : UuidGenerator {
    var constantUuid: String? = null
    private var counter: Int = 0

    fun cleanup() {
        counter = 0
        constantUuid = null
    }

    override fun generate(): String = constantUuid ?: "${counter++}"
}
