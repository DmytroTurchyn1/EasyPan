package com.cook.easypan.easypan.data.mappers

import com.cook.easypan.easypan.data.dto.RecipeDto
import com.cook.easypan.easypan.data.dto.StepDescriptionDto
import com.cook.easypan.easypan.domain.Recipe
import com.cook.easypan.easypan.domain.StepDescription
import com.cook.easypan.easypan.domain.StepType

/**
 * Converts a [RecipeDto] to a [Recipe] domain model.
 *
 * Maps all fields from the DTO to the domain object, translating the difficulty integer to a string label ("Easy", "Medium", "Hard"). Throws [IllegalArgumentException] if the difficulty value is unrecognized.
 *
 * @return The corresponding [Recipe] domain object.
 * @throws IllegalArgumentException If the difficulty value is not 1, 2, or 3.
 */
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

/**
 * Converts a [StepDescriptionDto] to a [StepDescription] domain object.
 *
 * Maps the step type string to the corresponding [StepType] enum value.
 * 
 * @return The resulting [StepDescription] domain object.
 * @throws IllegalArgumentException if the step type is unrecognized.
 */
fun StepDescriptionDto.toStepDescription(): StepDescription {
    return StepDescription(
        title = title,
        description = description,
        step = step,
        imageUrl = imageUrl,
        stepType = when (stepType) {
            "TEXT" -> StepType.TEXT
            "TIMER" -> StepType.TIMER
            else -> throw IllegalArgumentException("Unknown step type: $stepType")
        },
        durationSec = durationSec
    )
}