package anki

class SynchronizeUserUseCase(
    private val view: AnkiView,
    private val networkRepository: AnkiNetworkRepository,
    private val userRepository: AnkiUserRepository,
) {

    suspend fun start() {
        try {
            val user = networkRepository.fetchUser()
            if (user != null) {
                userRepository.updateUser(user)
            }
        } catch (e: AnkiApiException) {
            val dialog = AnkiDialog(
                title = "User synchronization exception",
                text = e.message,
                okButton = AnkiDialog.Button("OK")
            )
            view.show(dialog)
            return
        }

        val dialog = AnkiDialog(
            title = "Success",
            text = "User synchronization successful",
            okButton = AnkiDialog.Button("OK"),
        )
        view.show(dialog)
    }
}
