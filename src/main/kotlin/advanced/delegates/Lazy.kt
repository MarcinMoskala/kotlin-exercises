package advanced.delegates.lazy

data class BlogPost(
    val title: String,
    val content: String,
    val author: Author,
) {
    // TODO: Add properties here

    private fun generateSummary(content: String): String =
        content.take(100) + "..."
}

data class Author(
    val key: String,
    val name: String,
    val surname: String,
)

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
//   println(post.authorName)
//   // Alex Smith
//   println(post.wordCount)
//   // 153
//   println(post.isLongRead)
//   // true
//   println(post.summary)
//   // Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed non risus. Suspendisse lectus tortor, dignissim sit amet, adipiscing nec, ultricies sed, dolor. Cras elementum ultrices diam. Maecenas ligula massa, varius a, semper congue, euismod non, mi. Proin portt...
}
