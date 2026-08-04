package com.cook.easypan.easypan.domain.model

import java.time.LocalDate

/**
 * A generated weekly meal plan shown on the review screen.
 *
 * @param startDate the first day of the plan.
 * @param days one entry per planned day, in order.
 * @param preferences the answers this plan was generated from (surfaced as chips on review).
 */
data class MealPlan(
    val startDate: LocalDate,
    val days: List<PlanDay>,
    val preferences: MealPlanPreferences,
)

data class PlanDay(
    val date: LocalDate,
    val meals: List<Recipe>,
)
