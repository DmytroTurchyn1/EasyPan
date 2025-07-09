package com.dmytro_turchyn.easypan.easypan.domain

import com.dmytro_turchyn.easypan.easypan.presentation.home.components.RecipeComplexity

data class Recipe(
    val id: String,
    val title: String,
    val ingredients: List<String>,
    val preparationMinutes: Int,
    val cookMinutes: Int,
    val chips: List<String> = emptyList(),
    val difficulty: RecipeComplexity,
    val instructions: List<StepDescription>,
    val titleImg: String
)
