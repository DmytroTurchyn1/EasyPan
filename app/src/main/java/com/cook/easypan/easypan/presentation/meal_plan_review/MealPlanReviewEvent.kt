package com.cook.easypan.easypan.presentation.meal_plan_review

import com.cook.easypan.easypan.domain.model.Recipe

sealed interface MealPlanReviewEvent {
    data object Continue : MealPlanReviewEvent
    data object Dismiss : MealPlanReviewEvent
    data object EditPlan : MealPlanReviewEvent
    data class OpenRecipe(val recipe: Recipe) : MealPlanReviewEvent
}
