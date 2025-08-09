package com.cook.easypan.easypan.domain.model

import com.cook.easypan.core.domain.StepType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecipeTest {

    @Test
    fun `test recipe values`() {

        val instructions = listOf(
            StepDescription(
                description = "Step 1: Prepare ingredients",
                step = 1,
                imageUrl = "https://example.com/image.jpg",
                title = "Prepare ingredients",
                stepType = StepType.TEXT,
                durationSec = 100,
            ),
            StepDescription(
                description = "Step 2",
                step = 2,
                imageUrl = "https://example.com/image.jpg",
                title = "ingredients",
                stepType = StepType.TIMER,
                durationSec = 200,
            )
        )

        val recipe = Recipe(
            id = "testRecipeId",
            title = "testRecipe",
            ingredients = listOf("ingredient1", "ingredient2"),
            preparationMinutes = 10,
            cookMinutes = 20,
            chips = listOf("chip1", "chip2"),
            difficulty = "Easy",
            instructions = instructions,
            titleImg = "https://example.com/image.jpg"
        )

        assertEquals("testRecipeId", recipe.id)
        assertEquals("testRecipe", recipe.title)
        assertEquals(listOf("ingredient1", "ingredient2"), recipe.ingredients)
        assertEquals(10, recipe.preparationMinutes)
        assertEquals(20, recipe.cookMinutes)
        assertEquals(listOf("chip1", "chip2"), recipe.chips)
        assertEquals("Easy", recipe.difficulty)
        assertEquals(2, recipe.instructions.size)
    }

    @Test
    fun `test recipe with empty chips list uses default empty list`() {
        val recipe = Recipe(
            id = "testRecipeId",
            title = "testRecipe",
            ingredients = listOf("ingredient1"),
            preparationMinutes = 10,
            cookMinutes = 20,
            difficulty = "Easy",
            instructions = emptyList(),
            titleImg = "https://example.com/image.jpg"
        )

        assertTrue(recipe.chips.isEmpty())
        assertEquals(emptyList<String>(), recipe.chips)
    }

    @Test
    fun `test recipe with empty ingredients and instructions list`() {
        val recipe = Recipe(
            id = "testRecipeId",
            title = "testRecipe",
            ingredients = emptyList(),
            preparationMinutes = 10,
            cookMinutes = 20,
            difficulty = "Easy",
            instructions = emptyList(),
            titleImg = "https://example.com/image.jpg"
        )

        assertTrue(recipe.ingredients.isEmpty())
        assertTrue(recipe.instructions.isEmpty())

    }

    @Test
    fun `test recipe with large number of ingredients`() {
        val manyIngredients = (1..100).map { "ingredient$it" }
        val recipe = Recipe(
            id = "testRecipeId",
            title = "testRecipe",
            ingredients = manyIngredients,
            preparationMinutes = 10,
            cookMinutes = 20,
            difficulty = "Easy",
            instructions = emptyList(),
            titleImg = "https://example.com/image.jpg"
        )

        assertEquals(100, recipe.ingredients.size)
        assertEquals("ingredient1", recipe.ingredients.first())
        assertEquals("ingredient100", recipe.ingredients.last())
    }

    @Test
    fun `test recipe with multiple instructions`() {
        val instructions = listOf(
            StepDescription(
                description = "Step 1: Prepare ingredients",
                step = 1,
                imageUrl = "https://example.com/image.jpg",
                title = "Prepare ingredients",
                stepType = StepType.TEXT,
                durationSec = 100,
            ),
            StepDescription(
                description = "Step 2",
                step = 2,
                imageUrl = "https://example.com/image.jpg",
                title = "ingredients",
                stepType = StepType.TIMER,
                durationSec = 200,
            )
        )

        val recipe = Recipe(
            id = "testRecipeId",
            title = "testRecipe",
            ingredients = emptyList(),
            preparationMinutes = 10,
            cookMinutes = 20,
            difficulty = "Easy",
            instructions = instructions,
            titleImg = "https://example.com/image.jpg"
        )

        assertEquals(1, recipe.instructions.first().step)
        assertEquals("Prepare ingredients", recipe.instructions.first().title)

        assertEquals(2, recipe.instructions[1].step)
        assertEquals("ingredients", recipe.instructions[1].title)
    }

}