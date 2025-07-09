package com.dmytro_turchyn.easypan.easypan.presentation.home

import com.dmytro_turchyn.easypan.easypan.domain.Recipe

sealed interface HomeAction {
    data class OnRecipeClick(val recipe: Recipe) : HomeAction
}