package com.cook.easypan.easypan.presentation.recipe_step

sealed interface RecipeStepEvent {
    data class StartTimer(val durationMs: Long, val ownerStep: Int) : RecipeStepEvent
    data object PauseTimer : RecipeStepEvent
    data object StopTimer : RecipeStepEvent
}
