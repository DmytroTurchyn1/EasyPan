package com.cook.easypan.easypan.data.repository

import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.dto.RecipeDto
import com.cook.easypan.easypan.data.dto.StepDescriptionDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DefaultRecipeRepositoryTest {

    @RelaxedMockK
    private lateinit var firestoreDataSource: FirestoreClient

    private lateinit var defaultRecipeRepository: DefaultRecipeRepository

    @Before
    fun setUp() {
        io.mockk.MockKAnnotations.init(this)
        defaultRecipeRepository = DefaultRecipeRepository(firestoreDataSource)
    }

    @Test
    fun `getRecipes success with empty list`() = runBlocking {
        coEvery { firestoreDataSource.getRecipes() } returns emptyList()

        val recipes = defaultRecipeRepository.getRecipes()

        assertTrue(recipes.isEmpty())
    }

    @Test
    fun `getRecipes success with non empty list`() = runBlocking {
        val mockRecipeDto = mockk<RecipeDto>(relaxed = true) {
            every { difficulty } returns 1
        }
        val mockFirestoreRecipes = listOf(mockRecipeDto)

        coEvery { firestoreDataSource.getRecipes() } returns mockFirestoreRecipes

        val recipes = defaultRecipeRepository.getRecipes()

        assertTrue(recipes.isNotEmpty())
        assertTrue(recipes.size == mockFirestoreRecipes.size)
    }

    @Test
    fun `getRecipes firestoreDataSource throws exception`() {
        coEvery { firestoreDataSource.getRecipes() } throws Exception("Firestore error")

        try {
            runBlocking {
                defaultRecipeRepository.getRecipes()
            }
        } catch (e: Exception) {
            assertTrue(e.message == "Firestore error")
        }
    }

    @Test
    fun `getRecipes mapping toRecipe throws exception`() {
        val invalidRecipeDto = mockk<RecipeDto>(relaxed = true)
        val mockFirestoreRecipes = listOf(invalidRecipeDto)

        coEvery { firestoreDataSource.getRecipes() } returns mockFirestoreRecipes

        try {
            runBlocking {
                defaultRecipeRepository.getRecipes()
            }

            assertTrue("Expected IllegalArgumentException to be thrown", false)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Unknown difficulty level: 0") == true)
        }
    }

    @Test
    fun `getRecipes with partially invalid data from Firestore`() {
        val validRecipeDto = mockk<RecipeDto>(relaxed = true) {
            every { difficulty } returns 1
            every { instructions } returns emptyList()
        }

        val invalidRecipeDto = mockk<RecipeDto>(relaxed = true) {
            every { difficulty } returns 0 // Invalid difficulty
            every { instructions } returns emptyList()
        }

        val mockFirestoreRecipes = listOf(validRecipeDto, invalidRecipeDto)

        coEvery { firestoreDataSource.getRecipes() } returns mockFirestoreRecipes

        assertFailsWith<IllegalArgumentException> {
            runBlocking {
                defaultRecipeRepository.getRecipes()
            }
        }
    }

    @Test
    fun `getRecipes coroutine cancellation`() = runTest {
        coEvery { firestoreDataSource.getRecipes() } coAnswers {
            delay(1000)
            emptyList()
        }

        val job = launch {
            defaultRecipeRepository.getRecipes()
        }

        job.cancel()

        assertTrue(job.isCancelled)
    }

    @Test
    fun `getRecipes mapping toRecipe returns null for an item if toRecipe can return nullable`() {


        val validRecipeDto = mockk<RecipeDto>(relaxed = true) {
            every { difficulty } returns 1
            every { instructions } returns emptyList()
        }

        coEvery { firestoreDataSource.getRecipes() } returns listOf(validRecipeDto)

        val result = runBlocking {
            defaultRecipeRepository.getRecipes()
        }

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `getRecipes with a very large number of recipes`() = runBlocking {
        val largeRecipeList = (1..1000).map { index ->
            mockk<RecipeDto>(relaxed = true) {
                every { id } returns "recipe_$index"
                every { title } returns "Recipe $index"
                every { difficulty } returns 1
                every { ingredients } returns listOf("ingredient1", "ingredient2")
                every { instructions } returns emptyList()
            }
        }

        coEvery { firestoreDataSource.getRecipes() } returns largeRecipeList

        val result = defaultRecipeRepository.getRecipes()

        assertEquals(1000, result.size)
        assertEquals("recipe_1", result.first().id)
        assertEquals("recipe_1000", result.last().id)
    }

    @Test
    fun `getRecipes verify interaction with FirestoreClient mock`() = runBlocking {
        val mockRecipeDto = mockk<RecipeDto>(relaxed = true) {
            every { difficulty } returns 1
            every { instructions } returns emptyList()
        }

        coEvery { firestoreDataSource.getRecipes() } returns listOf(mockRecipeDto)

        defaultRecipeRepository.getRecipes()

        coVerify(exactly = 1) { firestoreDataSource.getRecipes() }
    }

    @Test
    fun `getRecipes verify transformation logic for each item`() = runBlocking {
        val stepDto = StepDescriptionDto(
            step = 1,
            title = "Test Step",
            description = "Test Description",
            imageUrl = "http://example.com/image.jpg",
            stepType = "TEXT",
            durationSec = null
        )

        val inputRecipeDto = RecipeDto(
            id = "test_id",
            title = "Test Recipe",
            ingredients = listOf("ingredient1", "ingredient2"),
            preparationMinutes = 15,
            cookMinutes = 30,
            chips = listOf("chip1", "chip2"),
            difficulty = 2,
            instructions = listOf(stepDto),
            titleImg = "http://example.com/title.jpg"
        )

        coEvery { firestoreDataSource.getRecipes() } returns listOf(inputRecipeDto)

        val result = defaultRecipeRepository.getRecipes()

        assertEquals(1, result.size)
        val recipe = result.first()

        assertEquals("test_id", recipe.id)
        assertEquals("Test Recipe", recipe.title)
        assertEquals(listOf("ingredient1", "ingredient2"), recipe.ingredients)
        assertEquals(15, recipe.preparationMinutes)
        assertEquals(30, recipe.cookMinutes)
        assertEquals(listOf("chip1", "chip2"), recipe.chips)
        assertEquals("Medium", recipe.difficulty)
        assertEquals(1, recipe.instructions.size)
        assertEquals("Test Step", recipe.instructions.first().title)
        assertEquals("http://example.com/title.jpg", recipe.titleImg)
    }
}