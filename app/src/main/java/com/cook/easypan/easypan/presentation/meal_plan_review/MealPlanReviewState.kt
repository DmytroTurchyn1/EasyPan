package com.cook.easypan.easypan.presentation.meal_plan_review

import androidx.compose.runtime.Stable
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.easypan.domain.model.Recipe

@Stable
data class MealPlanReviewState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val weekRange: String = "",
    val people: Int = 0,
    val mealsDay: Int = 0,
    val allergies: List<String> = emptyList(),
    val days: List<PlanDayUi> = emptyList(),
)

data class PlanDayUi(
    val dayName: String,
    val dateLabel: String,
    val meals: List<Recipe>,
)
