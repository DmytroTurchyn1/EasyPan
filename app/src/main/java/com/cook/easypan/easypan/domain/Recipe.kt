package com.cook.easypan.easypan.domain


data class Recipe(
    val id: String,
    val title: String,
    val ingredients: List<String>,
    val preparationMinutes: Int,
    val cookMinutes: Int,
    val chips: List<String> = emptyList(),
    val difficulty: String,
    val instructions: List<StepDescription>,
    val titleImg: String
)
