/*
 * Created  14/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

import com.cook.easypan.easypan.domain.model.User

data class ProfileState(
    val isSignedOut: Boolean = false,
    val recipesCooked: Int = 0,
    val favoriteCuisines: Int = 0,
    val isLoading: Boolean = true,
    val currentUser: User? = null,
    val keepScreenOn: Boolean = true
)