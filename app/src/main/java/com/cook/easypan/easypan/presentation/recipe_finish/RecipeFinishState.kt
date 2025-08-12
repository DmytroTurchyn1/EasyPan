/*
 * Created  13/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.recipe_finish

import com.cook.easypan.easypan.domain.model.Recipe

data class RecipeFinishState(
    val recipe: Recipe? = null,
    val userFinishedRecipes: Int = 0,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false
)