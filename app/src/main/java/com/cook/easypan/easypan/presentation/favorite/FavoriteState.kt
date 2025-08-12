/*
 * Created  13/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.favorite

import androidx.compose.foundation.lazy.LazyListState
import com.cook.easypan.easypan.domain.model.Recipe

data class FavoriteState(
    val favoriteRecipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
    val recipeListState: LazyListState = LazyListState()
)