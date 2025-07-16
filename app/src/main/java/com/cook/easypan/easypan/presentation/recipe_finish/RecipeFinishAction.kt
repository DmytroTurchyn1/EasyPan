package com.cook.easypan.easypan.presentation.recipe_finish

sealed interface RecipeFinishAction {
    data object OnFinishClick : RecipeFinishAction

}