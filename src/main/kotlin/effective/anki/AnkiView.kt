package anki

interface AnkiView {
    fun show(element: AnkiViewElement)
    fun hide(element: AnkiViewElement)
}

sealed class AnkiViewElement
data class AnkiProgressBar(val size: Size): AnkiViewElement() {
    enum class Size { Small, Large }
}
data class AnkiDialog(
    val title: String,
    val text: String,
    val okButton: AnkiDialog.Button? = null,
    val cancelButton: AnkiDialog.Button? = null,
    val onClose: (()->Unit)? = null
): AnkiViewElement() {
    data class Button(val text: String, val action: (()->Unit)? = null)
}
