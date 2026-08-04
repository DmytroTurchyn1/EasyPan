package com.cook.easypan.easypan.presentation.meal_plan

import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import com.cook.easypan.easypan.domain.model.Recipe

sealed interface MealPlanAction {
    data class OnGenerate(val preferences: MealPlanPreferences) : MealPlanAction
    data object OnRetry : MealPlanAction
    data class OnDaySelected(val index: Int) : MealPlanAction
    data class OnRecipeClick(val recipe: Recipe) : MealPlanAction
    data object OnRegenerateClick : MealPlanAction
    data object OnEditClick : MealPlanAction
    data object OnCreatePlanClick : MealPlanAction
}
