package com.cook.easypan.easypan.presentation.recipe_detail

import com.cook.easypan.easypan.domain.Recipe

sealed interface RecipeDetailAction {

    data class OnRecipeChange(val recipe: Recipe) : RecipeDetailAction

    data object OnStartRecipeClick: RecipeDetailAction

    data class OnIngredientCheck(val ingredientIndex: Int): RecipeDetailAction

    data object OnBackClick: RecipeDetailAction
}