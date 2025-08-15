package com.cook.easypan.easypan.presentation.recipe_step

import com.cook.easypan.easypan.domain.model.Recipe

sealed interface RecipeStepAction {
    data object OnPreviousClick : RecipeStepAction
    data object OnNextClick : RecipeStepAction
    data object OnFinishClick : RecipeStepAction
    data object OnDismissDialog : RecipeStepAction
    data object OnShowDialog : RecipeStepAction
    data object OnCancelClick : RecipeStepAction
    data class OnRecipeChange(val recipe: Recipe) : RecipeStepAction

}