package com.cook.easypan.easypan.presentation.recipe_step

import com.cook.easypan.easypan.domain.Recipe

sealed interface RecipeStepAction {
    data object OnPreviousClick : RecipeStepAction
    data object OnNextClick : RecipeStepAction
    data object OnBackClick : RecipeStepAction
    data object OnStartTimerClick : RecipeStepAction
    data object OnStopTimerClick : RecipeStepAction
    data class OnRecipeChange(val recipe: Recipe) : RecipeStepAction

}