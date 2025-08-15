package com.cook.easypan.easypan.data.dto

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

@Serializable
data class StepDescriptionDto(
    @PropertyName("step") val step: Int = 0,
    @PropertyName("imageUrl") val imageUrl: String? = null,
    @PropertyName("title") val title: String = "",
    @PropertyName("description") val description: String = "",
    @PropertyName("stepType") val stepType: String = "text",
    @PropertyName("durationSec") val durationSec: Int? = null
)