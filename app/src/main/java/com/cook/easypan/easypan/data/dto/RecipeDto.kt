package com.cook.easypan.easypan.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeDto (
    val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("ingredients") val ingredients: List<String> = emptyList(),
    @SerialName("preparationMinutes") val preparationMinutes: Int = 0,
    @SerialName("cookMinutes") val cookMinutes: Int = 0,
    @SerialName("chips") val chips: List<String> = emptyList(),
    @SerialName("difficulty") val difficulty: Int = 1,
    @SerialName("instructions") val instructions: List<StepDescriptionDto> = emptyList(),
    @SerialName("titleImg") val titleImg: String = ""
)