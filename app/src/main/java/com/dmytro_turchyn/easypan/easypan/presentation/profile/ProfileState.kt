data class ProfileState(
    val isSignedOut: Boolean = false,
    val recipesCooked : Int = 0,
    val favoriteCuisines: Int = 0,
    val notificationsEnabled: Boolean = false,
)