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

fun Recipe.toRecipeDto(): RecipeDto {
    return RecipeDto(
        id = id,
        title = title,
        ingredients = ingredients,
        preparationMinutes = preparationMinutes,
        cookMinutes = cookMinutes,
        chips = chips,
        difficulty = when (difficulty) {
            "Easy" -> 1
            "Medium" -> 2
            "Hard" -> 3
            else -> throw IllegalArgumentException("Unknown difficulty level: $difficulty")
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
            else -> throw IllegalArgumentException("Unknown step type: $stepType")
        },
        durationSec = durationSec?.also { require(it >= 0) { "Duration cannot be negative $it" } }
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
        durationSec = when {
            durationSec == null -> 0
            (durationSec < 0) -> throw IllegalArgumentException("Duration cannot be negative: $durationSec")
            else -> durationSec
        }
    )
}