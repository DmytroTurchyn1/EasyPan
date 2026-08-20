package com.cook.easypan.easypan.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MealPlanDto(
    val startDate: String = "",
    val days: List<PlanDayDto> = emptyList(),
    val preferences: MealPlanPreferencesDto = MealPlanPreferencesDto(),
)

@Serializable
data class PlanDayDto(
    val date: String = "",
    val recipeIds: List<String> = emptyList(),
)

@Serializable
data class MealPlanPreferencesDto(
    val favoriteProducts: List<String> = emptyList(),
    val skipProducts: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val mealsDay: Int = 3,
    val people: Int = 1,
)
