package com.cook.easypan.easypan.presentation.meal_plan

sealed interface MealPlanAction {
    data object OnCreatePlanClick : MealPlanAction
}
