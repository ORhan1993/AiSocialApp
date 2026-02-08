data class Post(
    val id: String,
    val username: String,
    val imageUrl: String,
    val description: String,
    val likeCount: Int,
    val isAiGenerated: Boolean = false // Yapay zeka etiketi için
)