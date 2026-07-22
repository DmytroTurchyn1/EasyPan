package com.cook.easypan.easypan.presentation.meal_plan_review

import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import com.cook.easypan.easypan.domain.model.Recipe

sealed interface MealPlanReviewAction {
    data class OnGenerate(val preferences: MealPlanPreferences) : MealPlanReviewAction
    data object OnRetry : MealPlanReviewAction
    data class OnRecipeClick(val recipe: Recipe) : MealPlanReviewAction
    data object OnContinueClick : MealPlanReviewAction
    data object OnEditClick : MealPlanReviewAction
    data object OnDismissPlanClick : MealPlanReviewAction
}
