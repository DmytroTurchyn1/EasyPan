package com.cook.easypan.easypan.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StepDescriptionDto(
    @SerialName("step") val step: Int = 0,
    @SerialName("imageUrl") val imageUrl: String? = null,
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("stepType") val stepType: String = "",
    @SerialName("durationSec") val durationSec: Int? = null
)