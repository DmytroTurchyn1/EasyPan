package com.dmytro_turchyn.easypan.easypan.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StepDescriptionDto (
    @SerialName("step") val step: String = "",
    @SerialName("imageUrl") val imageUrl: String? = null,
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("stepType") val stepType: String = "",
    @SerialName("durationSec") val durationSec: Int? = null
)