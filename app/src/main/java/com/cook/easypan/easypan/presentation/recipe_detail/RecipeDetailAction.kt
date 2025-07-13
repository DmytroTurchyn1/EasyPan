package com.cook.easypan.easypan.presentation.recipe_detail

import com.cook.easypan.easypan.domain.Recipe

sealed interface RecipeDetailAction {
    data class OnRecipeChange( val recipe: Recipe) : RecipeDetailAction
    data object OnBackClick: RecipeDetailAction
}