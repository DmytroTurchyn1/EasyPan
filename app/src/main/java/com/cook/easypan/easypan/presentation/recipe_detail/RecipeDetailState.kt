package com.cook.easypan.easypan.presentation.recipe_detail

import com.cook.easypan.easypan.domain.Recipe

data class RecipeDetailState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = true,
    val onIngredientCheckClicked : Boolean = false,
)