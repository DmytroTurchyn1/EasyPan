package com.cook.easypan.easypan.presentation.meal_plan

import com.cook.easypan.easypan.domain.model.Recipe

sealed interface MealPlanEvent {
    data object OpenWizard : MealPlanEvent
    data class OpenRecipe(val recipe: Recipe) : MealPlanEvent
}
