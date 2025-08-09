package com.cook.easypan.easypan.data.dto

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecipeDtoTest {

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
                    stepType = "Text",
                    durationSec = 60
                )
            ),
            chips = listOf("Chip1", "Chip2"),
            difficulty = 2
        )
    }

    @Test
    fun `test RecipeDto default values`() {
        val emptyRecipeDto = RecipeDto()

        assertEquals("", emptyRecipeDto.id)
        assertEquals("", emptyRecipeDto.title)
        assertEquals("", emptyRecipeDto.titleImg)
        assertEquals(0, emptyRecipeDto.preparationMinutes)
        assertEquals(0, emptyRecipeDto.cookMinutes)
        assertTrue(emptyRecipeDto.ingredients.isEmpty())
        assertTrue(emptyRecipeDto.instructions.isEmpty())
        assertTrue(emptyRecipeDto.chips.isEmpty())

    }

    @Test
    fun `test RecipeDto custom values`() {

        assertEquals("test_id", recipeDto.id)
        assertEquals("Test Recipe", recipeDto.title)
        assertEquals("test_image_url", recipeDto.titleImg)
        assertEquals(30, recipeDto.preparationMinutes)
        assertEquals(45, recipeDto.cookMinutes)
        assertEquals(listOf("Ingredient 1", "Ingredient 2"), recipeDto.ingredients)
        assertEquals(1, recipeDto.instructions.size)
        assertEquals("Test Step Title", recipeDto.instructions[0].title)
        assertEquals("Test Step Description", recipeDto.instructions[0].description)
        assertEquals("Text", recipeDto.instructions[0].stepType)
        assertEquals("test_image_url", recipeDto.instructions[0].imageUrl)
        assertEquals(listOf("Chip1", "Chip2"), recipeDto.chips)
        assertEquals(2, recipeDto.difficulty)
    }

    @Test
    fun `test all PropertyName annotations are correctly set`() {
        val expectedMappings = mapOf(
            "title" to "title",
            "ingredients" to "ingredients",
            "preparationMinutes" to "preparationMinutes",
            "cookMinutes" to "cookMinutes",
            "chips" to "chips",
            "difficulty" to "difficulty",
            "instructions" to "instructions",
            "titleImg" to "titleImg"
        )

        expectedMappings.forEach { (fieldName, expectedValue) ->
            val propertyAnnotation = RecipeDto::class.java
                .getDeclaredField(fieldName)
                .getAnnotation(PropertyName::class.java)

            assertNotNull("PropertyName annotation missing for $fieldName", propertyAnnotation)
            assertEquals(
                "PropertyName value incorrect for $fieldName",
                expectedValue, propertyAnnotation!!.value
            )
        }
    }

    @Test
    fun `test serialization and deserialization`() {

        val json = Json.encodeToString(RecipeDto.serializer(), recipeDto)
        val deserialized = Json.decodeFromString(RecipeDto.serializer(), json)

        assertEquals(recipeDto, deserialized)
        assertEquals("test_id", deserialized.id)
        assertEquals("Test Recipe", deserialized.title)
        assertEquals("test_image_url", deserialized.titleImg)
        assertEquals(30, deserialized.preparationMinutes)
        assertEquals(45, deserialized.cookMinutes)
        assertEquals(listOf("Ingredient 1", "Ingredient 2"), deserialized.ingredients)
        assertEquals(1, deserialized.instructions.size)
        assertEquals("Test Step Title", deserialized.instructions[0].title)
        assertEquals("Test Step Description", deserialized.instructions[0].description)
        assertEquals("Text", deserialized.instructions[0].stepType)
        assertEquals("test_image_url", deserialized.instructions[0].imageUrl)
        assertEquals(listOf("Chip1", "Chip2"), deserialized.chips)
        assertEquals(2, deserialized.difficulty)
    }


}