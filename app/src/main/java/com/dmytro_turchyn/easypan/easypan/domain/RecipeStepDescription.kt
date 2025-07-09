package com.dmytro_turchyn.easypan.easypan.domain

data class StepDescription(
    val step: String,
    val imageUrl: String? = null,
    val title: String,
    val description: String,
    val stepType: StepType,
    val durationSec: Int? = null
)
enum class StepType{
    TEXT,
    TIMER
}
