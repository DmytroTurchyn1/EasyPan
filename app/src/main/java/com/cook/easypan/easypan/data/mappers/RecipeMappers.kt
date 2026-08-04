/*
 * Created  15/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.mappers

import com.cook.easypan.core.domain.StepType
import com.cook.easypan.easypan.data.dto.IngredientDto
import com.cook.easypan.easypan.data.dto.RecipeDto
import com.cook.easypan.easypan.data.dto.StepDescriptionDto
import com.cook.easypan.easypan.domain.model.Ingredient
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.model.StepDescription
import com.google.firebase.firestore.DocumentSnapshot

// Server data must never crash the client: unknown values map to safe
// fallbacks instead of throwing.
fun RecipeDto.toRecipe(): Recipe {
    return Recipe(
        id = id,
        title = title,
        ingredients = ingredients.map { it.toIngredient() },
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
        ingredients = ingredients.map { it.toIngredientDto() },
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

fun IngredientDto.toIngredient(): Ingredient {
    return Ingredient(name = name, quantity = quantity)
}

fun Ingredient.toIngredientDto(): IngredientDto {
    return IngredientDto(name = name, quantity = quantity)
}

/**
 * Reads a recipe document field by field instead of via `toObject`, because `ingredients` has two
 * shapes in the wild: the pre-migration array of plain strings, and the current array of
 * `{name, quantity}` maps. Firestore's reflective deserializer throws on the mismatch, so the field
 * is normalized by hand — see `scripts/migrate_ingredients.py`.
 */
fun DocumentSnapshot.toRecipeDto(): RecipeDto? {
    if (!exists()) return null
    return RecipeDto(
        id = id,
        title = getString("title").orEmpty(),
        ingredients = get("ingredients").toIngredientDtos(),
        preparationMinutes = getLong("preparationMinutes")?.toInt() ?: 0,
        cookMinutes = getLong("cookMinutes")?.toInt() ?: 0,
        chips = get("chips").toStringList(),
        difficulty = getLong("difficulty")?.toInt() ?: 1,
        instructions = get("instructions").toStepDescriptionDtos(),
        allergies = get("allergies").toStringList(),
        titleImg = getString("titleImg").orEmpty()
    )
}

private fun Any?.toIngredientDtos(): List<IngredientDto> {
    return (this as? List<*>).orEmpty().mapNotNull { element ->
        when (element) {
            is String -> IngredientDto(name = element.trim())
            else -> element.asFirestoreMap()?.let { map ->
                IngredientDto(
                    name = (map["name"] as? String).orEmpty().trim(),
                    quantity = (map["quantity"] as? String).orEmpty().trim()
                )
            }
        }?.takeIf { it.name.isNotEmpty() }
    }
}

private fun Any?.toStepDescriptionDtos(): List<StepDescriptionDto> {
    return (this as? List<*>).orEmpty().mapNotNull { element ->
        element.asFirestoreMap()?.let { map ->
            StepDescriptionDto(
                step = (map["step"] as? Number)?.toInt() ?: 0,
                imageUrl = map["imageUrl"] as? String,
                title = (map["title"] as? String).orEmpty(),
                description = (map["description"] as? String).orEmpty(),
                stepType = (map["stepType"] as? String) ?: "text",
                durationSec = (map["durationSec"] as? Number)?.toInt()
            )
        }
    }
}

private fun Any?.toStringList(): List<String> {
    return (this as? List<*>).orEmpty()
        .mapNotNull { (it as? String)?.trim()?.takeIf(String::isNotEmpty) }
}

@Suppress("UNCHECKED_CAST")
private fun Any?.asFirestoreMap(): Map<String, Any?>? = this as? Map<String, Any?>

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
