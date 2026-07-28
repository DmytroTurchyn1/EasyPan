package com.cook.easypan.easypan.presentation.ingredients_receipt

import android.util.Log
import com.cook.easypan.easypan.domain.model.IngredientCategory
import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.repository.RecipeRepository
import com.cook.easypan.easypan.domain.usecase.BuildGroceriesListUseCase
import com.cook.easypan.easypan.domain.usecase.GenerateMealPlanUseCase
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class IngredientsReceiptViewModelTest {

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `OnGenerate populates the categorized list`() = runTest {
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()))

        viewModel.onAction(
            IngredientsReceiptAction.OnGenerate(MealPlanPreferences(mealsDay = 3, people = 2)),
        )

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(21, state.mealsCount)
        assertEquals(2, state.people)
        assertEquals(4, state.totalItems) // Chicken, Rice, Spinach, Hummus
        assertEquals(
            listOf(
                IngredientCategory.PRODUCE,
                IngredientCategory.MEAT_FISH,
                IngredientCategory.PANTRY,
                IngredientCategory.OTHER,
            ),
            state.categories.map { it.category },
        )
    }

    @Test
    fun `failure exposes an error instead of crashing`() = runTest {
        val repository = FakeRecipeRepository(defaultCatalog(), failFirst = true)
        val viewModel = viewModel(repository)

        viewModel.onAction(IngredientsReceiptAction.OnGenerate(MealPlanPreferences()))

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.categories.isEmpty())
    }

    @Test
    fun `OnRetry regenerates after a failure`() = runTest {
        val repository = FakeRecipeRepository(defaultCatalog(), failFirst = true)
        val viewModel = viewModel(repository)

        viewModel.onAction(IngredientsReceiptAction.OnGenerate(MealPlanPreferences()))
        assertNotNull(viewModel.state.value.error)

        viewModel.onAction(IngredientsReceiptAction.OnRetry)

        val state = viewModel.state.value
        assertNull(state.error)
        assertTrue(state.categories.isNotEmpty())
    }

    private fun viewModel(repository: RecipeRepository) = IngredientsReceiptViewModel(
        BuildGroceriesListUseCase(GenerateMealPlanUseCase(repository)),
    )

    private fun defaultCatalog(): List<Recipe> = listOf(
        Recipe(
            id = "r1",
            title = "Recipe 1",
            ingredients = listOf("Chicken", "Rice", "Spinach", "Hummus"),
            allergies = emptyList(),
            preparationMinutes = 10,
            cookMinutes = 20,
            chips = emptyList(),
            difficulty = "Easy",
            instructions = emptyList(),
            titleImg = "",
        ),
    )

    private class FakeRecipeRepository(
        private val recipes: List<Recipe>,
        private var failFirst: Boolean = false,
    ) : RecipeRepository {
        override suspend fun getRecipes(): List<Recipe> {
            if (failFirst) {
                failFirst = false
                throw RuntimeException("Firestore unavailable")
            }
            return recipes
        }
    }
}
