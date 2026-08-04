package com.cook.easypan.easypan.domain.model

data class MealPlanPreferences(
    val favoriteProducts: List<String> = emptyList(),
    val skipProducts: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val mealsDay: Int = 3,
    val people: Int = 1
)