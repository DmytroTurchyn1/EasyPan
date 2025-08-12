/*
 * Created  13/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.recipe_detail

import com.cook.easypan.easypan.domain.model.Recipe

sealed interface RecipeDetailAction {

    data class OnRecipeChange(val recipe: Recipe) : RecipeDetailAction
    data class OnFavoriteButtonClick(val recipeId: String) : RecipeDetailAction

    data object OnStartRecipeClick : RecipeDetailAction

    data class OnIngredientCheck(val ingredientIndex: Int) : RecipeDetailAction

    data object OnBackClick : RecipeDetailAction
}