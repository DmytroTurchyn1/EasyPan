package com.dmytro_turchyn.easypan.easypan.data.dto

import com.dmytro_turchyn.easypan.easypan.domain.StepType
import kotlinx.serialization.Serializable

@Serializable
data class StepDescriptionDto (
    val step: String,
    val imageUrl: String? = null,
    val title: String,
    val description: String,
    val stepType: String,
    val durationSec: Int? = null
)