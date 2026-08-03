package com.cook.easypan.easypan.domain.model

data class Recipe(
    val id: String,
    val title: String,
    val ingredients: List<Ingredient>,
    val allergies: List<String> = emptyList(),
    val preparationMinutes: Int,
    val cookMinutes: Int,
    val chips: List<String> = emptyList(),
    val difficulty: String,
    val instructions: List<StepDescription>,
    val titleImg: String
)