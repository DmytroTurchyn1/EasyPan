import com.cook.easypan.easypan.domain.model.User

data class ProfileState(
    val isSignedOut: Boolean = false,
    val recipesCooked: Int = 0,
    val favoriteCuisines: Int = 0,
    val isLoading: Boolean = true,
    val currentUser: User? = null
)