/*
 * Created  13/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.recipe_detail

import com.cook.easypan.easypan.domain.model.Recipe

data class RecipeDetailState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = true,
    val isFavorite: Boolean = false,
    val onIngredientCheckClicked: Set<Int> = emptySet(),
)