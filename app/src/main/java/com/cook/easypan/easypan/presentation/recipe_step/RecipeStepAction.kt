package com.cook.easypan.easypan.presentation.recipe_step

import com.cook.easypan.easypan.domain.Recipe
import com.cook.easypan.easypan.presentation.recipe_detail.RecipeDetailAction

sealed interface RecipeStepAction {
    data object OnPreviousClick : RecipeStepAction
    data object OnNextClick : RecipeStepAction
    data object OnBackClick : RecipeStepAction
    data class OnRecipeChange(val recipe: Recipe) : RecipeStepAction
}