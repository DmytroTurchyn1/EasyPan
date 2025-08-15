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
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class RecipeMappersTest {

    private lateinit var recipeDto: RecipeDto

    @Before
    fun setup() {
        recipeDto = RecipeDto(
            id = "test_id",
            title = "Test Recipe",
            titleImg = "test_image_url",
            preparationMinutes = 30,
            cookMinutes = 45,
            ingredients = listOf("Ingredient 1", "Ingredient 2"),
            instructions = listOf(
                StepDescriptionDto(
                    step = 1,
                    imageUrl = "test_image_url",
                    title = "Test Step Title",
                    description = "Test Step Description",
                    stepType = "TEXT",
                    durationSec = 60
                )
            ),
            chips = listOf("Chip1", "Chip2"),
            difficulty = 2
        )
    }

    @Test
    fun `toRecipe basic mapping`() {
        val recipe = recipeDto.toRecipe()

        assertEquals("test_id", recipe.id)
        assertEquals("Test Recipe", recipe.title)
        assertEquals("test_image_url", recipe.titleImg)
        assertEquals(30, recipe.preparationMinutes)
        assertEquals(45, recipe.cookMinutes)
        assertEquals(listOf("Ingredient 1", "Ingredient 2"), recipe.ingredients)
        assertEquals(listOf("Chip1", "Chip2"), recipe.chips)
        assertEquals("Medium", recipe.difficulty)
        assertEquals(1, recipe.instructions.size)
    }

    @Test
    fun `toRecipe difficulty mapping   Easy`() {
        val recipe = recipeDto.copy(difficulty = 1).toRecipe()

        assertEquals("Easy", recipe.difficulty)
    }

    @Test
    fun `toRecipe difficulty mapping   Medium`() {
        val recipe = recipeDto.copy(difficulty = 2).toRecipe()

        assertEquals("Medium", recipe.difficulty)
    }

    @Test
    fun `toRecipe difficulty mapping   Hard`() {
        val recipe = recipeDto.copy(difficulty = 3).toRecipe()

        assertEquals("Hard", recipe.difficulty)
    }

    @Test
    fun `toRecipe invalid difficulty`() {
        val recipe = recipeDto.copy(difficulty = 4)

        assertFailsWith<IllegalArgumentException> {
            recipe.toRecipe()
        }
    }

    @Test
    fun `toRecipe empty instructions list`() {
        val recipe = recipeDto.copy(instructions = emptyList()).toRecipe()

        assertEquals(emptyList(), recipe.instructions)
    }

    @Test
    fun `toRecipe instructions mapping`() {
        val recipe = recipeDto.toRecipe()
        assertEquals(1, recipe.instructions[0].step)
        assertEquals("Test Step Title", recipe.instructions[0].title)
        assertEquals("Test Step Description", recipe.instructions[0].description)
        assertEquals(StepType.TEXT, recipe.instructions[0].stepType)
        assertEquals("test_image_url", recipe.instructions[0].imageUrl)
        assertEquals(60, recipe.instructions[0].durationSec)
    }

    @Test
    fun `toRecipe null or empty fields`() {
        val recipe = RecipeDto().toRecipe()

        assertEquals("", recipe.id)
        assertEquals("", recipe.title)
        assertEquals("", recipe.titleImg)
        assertEquals(0, recipe.preparationMinutes)
        assertEquals(0, recipe.cookMinutes)
        assertEquals(emptyList(), recipe.ingredients)
        assertEquals(emptyList(), recipe.chips)
        assertEquals("Easy", recipe.difficulty)
        assertEquals(emptyList(), recipe.instructions)
    }

    @Test
    fun `toStepDescription basic mapping`() {
        val stepDescriptionDto = recipeDto.instructions.first()
        val stepDescription = stepDescriptionDto.toStepDescription()

        assertEquals("Test Step Title", stepDescription.title)
        assertEquals("Test Step Description", stepDescription.description)
        assertEquals(1, stepDescription.step)
        assertEquals("test_image_url", stepDescription.imageUrl)
        assertEquals(StepType.TEXT, stepDescription.stepType)
        assertEquals(60, stepDescription.durationSec)
    }

    @Test
    fun `toStepDescription stepType mapping   TEXT`() {
        val stepDescriptionDto = recipeDto.instructions.first()
        val stepDescription = stepDescriptionDto.toStepDescription()

        assertEquals(StepType.TEXT, stepDescription.stepType)
    }

    @Test
    fun `toStepDescription stepType mapping   TIMER`() {
        val stepDescriptionDto = recipeDto.instructions.first().copy(stepType = "TIMER")
        val stepDescription = stepDescriptionDto.toStepDescription()

        assertEquals(StepType.TIMER, stepDescription.stepType)
    }

    @Test
    fun `toStepDescription invalid stepType`() {
        val recipe = recipeDto.instructions.first().copy(stepType = "INVALID")

        assertFailsWith<IllegalArgumentException> {
            recipe.toStepDescription()
        }
    }

    @Test
    fun `toStepDescription case sensitivity of stepType`() {
        val recipeText = recipeDto.instructions.first().copy(stepType = "text")
        val recipeTimer = recipeDto.instructions.first().copy(stepType = "timer")
        val stepDescriptionText = recipeText.toStepDescription()
        val stepDescriptionTimer = recipeTimer.toStepDescription()

        assertEquals(StepType.TEXT, stepDescriptionText.stepType)
        assertEquals(StepType.TIMER, stepDescriptionTimer.stepType)
    }

    @Test
    fun `toStepDescription null or empty optional fields`() {
        val emptyStepDescription = StepDescriptionDto().toStepDescription()
        assertEquals("", emptyStepDescription.title)
        assertEquals("", emptyStepDescription.description)
        assertEquals(0, emptyStepDescription.step)
        assertNull(emptyStepDescription.imageUrl)
        assertNull(emptyStepDescription.durationSec)
    }

    @Test
    fun `toRecipe with large number of instructions`() {
        val largeInstructionsList = (1..1000).map { index ->
            StepDescriptionDto(
                step = index,
                imageUrl = "image_url_$index",
                title = "Step Title $index",
                description = "Step Description $index",
                stepType = if (index % 2 == 0) "TEXT" else "TIMER",
                durationSec = index * 10
            )
        }

        val largeRecipeDto = recipeDto.copy(instructions = largeInstructionsList)

        val startTime = System.currentTimeMillis()
        val recipe = largeRecipeDto.toRecipe()
        val endTime = System.currentTimeMillis()

        assertEquals(1000, recipe.instructions.size)
        assertEquals(1, recipe.instructions.first().step)
        assertEquals("Step Title 1", recipe.instructions.first().title)
        assertEquals(StepType.TIMER, recipe.instructions.first().stepType)
        assertEquals(1000, recipe.instructions.last().step)
        assertEquals("Step Title 1000", recipe.instructions.last().title)
        assertEquals(StepType.TEXT, recipe.instructions.last().stepType)

        val mappingTime = endTime - startTime
        assert(mappingTime < 1000) { "Mapping took too long: ${mappingTime}ms" }
    }

    @Test
    fun `toRecipe with all optional fields missing`() {
        val emptyRecipeDto = RecipeDto()

        val recipe = emptyRecipeDto.toRecipe()

        assertEquals("", recipe.id)
        assertEquals("", recipe.title)
        assertEquals("", recipe.titleImg)
        assertEquals(0, recipe.preparationMinutes)
        assertEquals(0, recipe.cookMinutes)
        assertEquals(emptyList(), recipe.ingredients)
        assertEquals(emptyList(), recipe.chips)
        assertEquals("Easy", recipe.difficulty)
        assertEquals(emptyList(), recipe.instructions)
    }

    @Test
    fun `toStepDescription with durationSec as zero or negative for TIMER type`() {
        val stepDescriptionDtoZero = StepDescriptionDto(stepType = "TIMER").copy(durationSec = 0)
        val negativeDurationStepDescriptionDto =
            StepDescriptionDto(stepType = "TIMER").copy(durationSec = -10)
        val stepDescriptionDtoNull = StepDescriptionDto(stepType = "TIMER").copy(durationSec = null)

        val stepDescriptionZero = stepDescriptionDtoZero.toStepDescription()
        val stepDescriptionNull = stepDescriptionDtoNull.toStepDescription()

        assertEquals(0, stepDescriptionZero.durationSec)
        assertNull(stepDescriptionNull.durationSec)
        assertFailsWith<IllegalArgumentException> {
            negativeDurationStepDescriptionDto.toStepDescription()
        }
    }

}