package com.cook.easypan.easypan.data.mappers

import com.cook.easypan.easypan.data.dto.MealPlanDto
import com.cook.easypan.easypan.data.dto.MealPlanPreferencesDto
import com.cook.easypan.easypan.data.dto.PlanDayDto
import com.cook.easypan.easypan.domain.model.MealPlan
import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import com.cook.easypan.easypan.domain.model.PlanDay
import com.cook.easypan.easypan.domain.model.Recipe
import java.time.LocalDate
import java.time.format.DateTimeParseException

fun MealPlan.toMealPlanDto(): MealPlanDto {
    return MealPlanDto(
        startDate = startDate.toString(),
        days = days.map { day ->
            PlanDayDto(
                date = day.date.toString(),
                recipeIds = day.meals.map { it.id }
            )
        },
        preferences = preferences.toMealPlanPreferencesDto()
    )
}

// Returns null when the stored plan can no longer be rebuilt faithfully — an unparsable date or a
// recipe that has since disappeared from the catalog. The caller regenerates instead of showing a
// week with holes in it.
fun MealPlanDto.toMealPlan(catalog: List<Recipe>): MealPlan? {
    val start = startDate.toLocalDateOrNull() ?: return null
    if (days.isEmpty()) return null

    val byId = catalog.associateBy { it.id }
    val planDays = days.map { dayDto ->
        val date = dayDto.date.toLocalDateOrNull() ?: return null
        if (dayDto.recipeIds.isEmpty()) return null
        val meals = dayDto.recipeIds.map { id -> byId[id] ?: return null }
        PlanDay(date = date, meals = meals)
    }

    return MealPlan(
        startDate = start,
        days = planDays,
        preferences = preferences.toMealPlanPreferences()
    )
}

fun MealPlanPreferences.toMealPlanPreferencesDto(): MealPlanPreferencesDto {
    return MealPlanPreferencesDto(
        favoriteProducts = favoriteProducts,
        skipProducts = skipProducts,
        allergies = allergies,
        mealsDay = mealsDay,
        people = people
    )
}

fun MealPlanPreferencesDto.toMealPlanPreferences(): MealPlanPreferences {
    return MealPlanPreferences(
        favoriteProducts = favoriteProducts,
        skipProducts = skipProducts,
        allergies = allergies,
        mealsDay = mealsDay,
        people = people
    )
}

private fun String.toLocalDateOrNull(): LocalDate? = try {
    LocalDate.parse(this)
} catch (e: DateTimeParseException) {
    null
}
