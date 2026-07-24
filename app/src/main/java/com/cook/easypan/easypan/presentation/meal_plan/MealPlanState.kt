package com.cook.easypan.easypan.presentation.meal_plan

import androidx.compose.runtime.Stable
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.easypan.domain.model.Recipe

@Stable
data class MealPlanState(
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val isProUser: Boolean = false,
    val weekRange: String = "",
    val people: Int = 0,
    val mealsDay: Int = 0,
    val days: List<MealPlanDayUi> = emptyList(),
    val selectedDayIndex: Int = 0,
) {
    val hasPlan: Boolean get() = days.isNotEmpty()
    val selectedDay: MealPlanDayUi? get() = days.getOrNull(selectedDayIndex)
    val upNextDay: MealPlanDayUi? get() = days.getOrNull(selectedDayIndex + 1)
}

data class MealPlanDayUi(
    val dayName: String,     // "Monday" — section header + Up Next card
    val dayAbbrev: String,   // "Mon"    — day chip top line
    val dayOfMonth: Int,     // 13       — day chip bottom line
    val meals: List<Recipe>,
)
