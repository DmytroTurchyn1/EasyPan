package com.cook.easypan.easypan.domain

data class StepDescription(
    val step: Int,
    val imageUrl: String? = null,
    val title: String,
    val description: String,
    val stepType: StepType,
    val durationSec: Int? = null
)