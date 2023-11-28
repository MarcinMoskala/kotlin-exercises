package domain.comment

class FakeUuidProvider: UuidProvider {
    private var counter = 1
    private var constantReturn: String? = null

    override fun next(): String = constantReturn ?: "UUID#" + (counter++)

    fun clean() {
        counter = 1
        constantReturn = null
    }

    fun alwaysReturn(value: String) {
        constantReturn = value
    }
} 