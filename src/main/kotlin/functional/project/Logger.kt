package functional.project

interface Logger {
    fun log(message: String)
}

class FakeLogger : Logger {
    var messages: List<String> = emptyList()

    fun cleanup() {
        messages = emptyList()
    }

    override fun log(message: String) {
        this.messages += message
    }
}
