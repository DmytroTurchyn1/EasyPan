package com.cook.easypan.easypan.presentation.recipe_step

import com.cook.easypan.easypan.domain.Recipe

data class RecipeStepState(
    val recipe: Recipe? = null,
    val step: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)