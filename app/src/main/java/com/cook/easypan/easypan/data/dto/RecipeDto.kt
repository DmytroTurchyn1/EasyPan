package com.cook.easypan.easypan.data.dto

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeDto(
    val id: String = "",
    @PropertyName("title") val title: String = "",
    @PropertyName("ingredients") val ingredients: List<String> = emptyList(),
    @PropertyName("preparationMinutes") val preparationMinutes: Int = 0,
    @PropertyName("cookMinutes") val cookMinutes: Int = 0,
    @PropertyName("chips") val chips: List<String> = emptyList(),
    @PropertyName("difficulty") val difficulty: Int = 1,
    @PropertyName("instructions") val instructions: List<StepDescriptionDto> = emptyList(),
    @PropertyName("titleImg") val titleImg: String = ""
)