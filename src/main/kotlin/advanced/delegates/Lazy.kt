package advanced.delegates.lazy

data class BlogPost(
    val title: String,
    val content: String,
    val author: Author,
) {
    // Since this property is needed on average more than
    // once per blog post, and it is not expensive to
    // calculate, it is best to define it as a value.
    val authorName: String =
        "${author.name} ${author.surname}"

    // Since this property is needed on average more than
    // once per blog post, and it is expensive to
    // calculate, it is best to define it as a lazy
    val wordCount: Int by lazy {
        content.split("\\s+").size
    }

    // Since this property is needed on average less than
    // once per blog post, and it is not expensive to
    // calculate, it is best to define it as a getter.
    val isLongRead: Boolean
        get() = content.length > 1000

    // Since this property is very expensive to calculate,
    // it is best to define it as a lazy
    val summary: String by lazy {
        generateSummary(content)
    }

    private fun generateSummary(content: String): String =
        content.take(100) + "..."
}

data class Author(
    val key: String,
    val name: String,
    val surname: String,
)

class BlogPostTest {

    fun main() {
        val post = BlogPost(
            title = "Hello, World!",
            content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                    "Sed non risus. Suspendisse lectus tortor, dignissim sit amet, " +
                    "adipiscing nec, ultricies sed, dolor. Cras elementum ultrices diam. " +
                    "Maecenas ligula massa, varius a, semper congue, euismod non, mi. " +
                    "Proin porttitor, orci nec nonummy molestie, enim est eleifend mi, " +
                    "non fermentum diam nisl sit amet erat. Duis semper. Duis arcu massa, " +
                    "scelerisque vitae, consequat in, pretium a, enim. Pellentesque congue. " +
                    "Ut in risus volutpat libero pharetra tempor. Cras vestibulum bibendum " +
                    "augue. Praesent egestas leo in pede. Praesent blandit odio eu enim. " +
                    "Pellentesque sed dui ut augue blandit sodales. Vestibulum ante ipsum " +
                    "primis in faucibus orci luctus et ultrices posuere cubilia Curae; " +
                    "Aliquam nibh. Mauris ac mauris sed pede pellentesque fermentum. " +
                    "Maecenas adipiscing ante non diam sodales hendrerit.",
            author = Author(
                key = "alex",
                name = "Alex",
                surname = "Smith",
            ),
        )
        println(post.authorName)
        // Alex Smith
        println(post.wordCount)
        // 153
        println(post.isLongRead)
        // true
        println(post.summary)
        // Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed non risus. Suspendisse lectus tortor, dignissim sit amet, adipiscing nec, ultricies sed, dolor. Cras elementum ultrices diam. Maecenas ligula massa, varius a, semper congue, euismod non, mi. Proin portt...
    }
}
