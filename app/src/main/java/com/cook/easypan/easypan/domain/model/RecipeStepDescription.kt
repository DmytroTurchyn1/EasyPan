package com.cook.easypan.easypan.domain.model

import com.cook.easypan.core.domain.StepType

data class StepDescription(
    val step: Int,
    val imageUrl: String? = null,
    val title: String,
    val description: String,
    val stepType: StepType,
    val durationSec: Int? = null
)