package com.dmytro_turchyn.easypan.easypan.data.dto

import com.dmytro_turchyn.easypan.easypan.domain.StepDescription
import com.dmytro_turchyn.easypan.easypan.presentation.home.components.RecipeComplexity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeDto (
    @SerialName("") val id: String,
    @SerialName("title") val title: String,
    @SerialName("ingredients") val ingredients: List<String>,
    @SerialName("preparationMinutes") val preparationMinutes: Int,
    @SerialName("cookMinutes") val cookMinutes: Int,
    @SerialName("chips") val chips: List<String> = emptyList(),
    @SerialName("difficulty") val difficulty: RecipeComplexity,
    @SerialName("instructions") val instructions: List<StepDescriptionDto>,
    @SerialName("titleImg")val titleImg: String
)