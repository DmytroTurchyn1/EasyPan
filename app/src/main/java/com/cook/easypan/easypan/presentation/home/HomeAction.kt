package com.cook.easypan.easypan.presentation.home

import com.cook.easypan.easypan.domain.Recipe

sealed interface HomeAction {
    data class OnRecipeClick(val recipe: Recipe) : HomeAction
}