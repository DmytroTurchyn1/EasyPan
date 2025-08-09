package com.cook.easypan.easypan.data.mappers

import com.cook.easypan.core.domain.StepType
import com.cook.easypan.easypan.data.dto.RecipeDto
import com.cook.easypan.easypan.data.dto.StepDescriptionDto
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.model.StepDescription

fun RecipeDto.toRecipe(): Recipe {
    return Recipe(
        id = id,
        title = title,
        ingredients = ingredients,
        preparationMinutes = preparationMinutes,
        cookMinutes = cookMinutes,
        chips = chips,
        difficulty = when (difficulty) {
            1 -> "Easy"
            2 -> "Medium"
            3 -> "Hard"
            else -> throw IllegalArgumentException("Unknown difficulty level: $difficulty")
        },
        instructions = instructions.map { it.toStepDescription() },
        titleImg = titleImg
    )
}

fun StepDescriptionDto.toStepDescription(): StepDescription {
    return StepDescription(
        title = title,
        description = description,
        step = step,
        imageUrl = imageUrl,
        stepType = when (stepType.uppercase()) {
            "TEXT" -> StepType.TEXT
            "TIMER" -> StepType.TIMER
            else -> throw IllegalArgumentException("Unknown step type: $stepType")
        },
        durationSec = when {
            durationSec == null -> 0
            (durationSec < 0) -> throw IllegalArgumentException("Duration cannot be negative: $durationSec")
            else -> durationSec
        }
    )
}