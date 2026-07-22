/*
 * Created  15/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.mappers

import com.cook.easypan.core.domain.StepType
import com.cook.easypan.easypan.data.dto.RecipeDto
import com.cook.easypan.easypan.data.dto.StepDescriptionDto
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.model.StepDescription

// Server data must never crash the client: unknown values map to safe
// fallbacks instead of throwing.
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
            else -> "Unknown"
        },
        instructions = instructions.map { it.toStepDescription() },
        titleImg = titleImg,
        allergies = allergies
    )
}

fun Recipe.toRecipeDto(): RecipeDto {
    return RecipeDto(
        id = id,
        title = title,
        ingredients = ingredients,
        preparationMinutes = preparationMinutes,
        cookMinutes = cookMinutes,
        chips = chips,
        allergies = allergies,
        difficulty = when (difficulty) {
            "Easy" -> 1
            "Medium" -> 2
            "Hard" -> 3
            else -> 0
        },
        instructions = instructions.map { it.toStepDescriptionDto() },
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
            else -> StepType.TEXT
        },
        durationSec = durationSec?.coerceAtLeast(0)
    )
}

fun StepDescription.toStepDescriptionDto(): StepDescriptionDto {
    return StepDescriptionDto(
        title = title,
        description = description,
        step = step,
        imageUrl = imageUrl,
        stepType = when (stepType) {
            StepType.TEXT -> "TEXT"
            StepType.TIMER -> "TIMER"
        },
        durationSec = durationSec?.coerceAtLeast(0) ?: 0
    )
}
