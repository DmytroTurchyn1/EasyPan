package com.cook.easypan.easypan.data.mappers

import com.cook.easypan.easypan.data.dto.RecipeDto
import com.cook.easypan.easypan.data.dto.StepDescriptionDto
import com.cook.easypan.easypan.domain.Recipe
import com.cook.easypan.easypan.domain.StepDescription
import com.cook.easypan.easypan.domain.StepType
import com.cook.easypan.easypan.presentation.home.components.RecipeComplexity

fun RecipeDto.toRecipe() : Recipe{
    return Recipe(
        id = id,
        title = title,
        ingredients = ingredients,
        preparationMinutes  =preparationMinutes,
        cookMinutes = cookMinutes,
        chips = chips,
        difficulty = when(difficulty) {
            1 -> RecipeComplexity.EASY
            2 -> RecipeComplexity.MEDIUM
            3 -> RecipeComplexity.HARD
            else -> throw IllegalArgumentException("Unknown difficulty level: $difficulty")
        },
        instructions = instructions.map { it.toStepDescription() },
        titleImg = titleImg
    )
}

fun StepDescriptionDto.toStepDescription() : StepDescription {
    return StepDescription(
        title = title,
        description = description,
        step = step,
        imageUrl = imageUrl,
        stepType = when(stepType){
            "TEXT" -> StepType.TEXT
            "TIMER" -> StepType.TIMER
            else -> throw IllegalArgumentException("Unknown step type: $stepType")
        },
    )
}