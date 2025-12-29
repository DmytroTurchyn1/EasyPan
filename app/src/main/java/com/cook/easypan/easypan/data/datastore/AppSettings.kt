/*
 * Created  26/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.datastore

import com.cook.easypan.easypan.data.dto.RecipeDto
import com.cook.easypan.easypan.data.dto.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val keepScreenOn: Boolean = true,
    val userId: String? = null,
    val userName: String? = null,
    val userPhotoUrl: String? = null,
    val cachedUserData: UserDto? = null,
    val cacheFavoriteRecipes: List<RecipeDto> = emptyList(),
    val cachedRecipes: List<RecipeDto> = emptyList(),
    val lastCacheTimeRecipes: Long = 0L,
    val lastCacheTimeFavorites: Long = 0L,
    val lastCacheTimeUserData: Long = 0L
)