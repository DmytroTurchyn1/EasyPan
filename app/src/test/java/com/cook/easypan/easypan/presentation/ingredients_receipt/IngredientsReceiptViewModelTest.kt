package com.cook.easypan.easypan.presentation.ingredients_receipt

import android.util.Log
import com.cook.easypan.easypan.domain.model.Ingredient
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
import kotlinx.coroutines.flow.first
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
import kotlin.test.assertSame
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

    @Test
    fun `OnGenerate flattens the ingredients and starts with nothing checked`() = runTest {
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()))

        viewModel.onAction(IngredientsReceiptAction.OnGenerate(MealPlanPreferences()))

        val state = viewModel.state.value
        assertEquals(
            listOf("Chicken", "Rice", "Spinach", "Hummus").sorted(),
            state.ingredients.map { it.name }.sorted(),
        )
        assertTrue(state.checkedIngredients.isEmpty())
    }

    @Test
    fun `OnCheckClick toggles an ingredient on and back off`() = runTest {
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()))
        viewModel.onAction(IngredientsReceiptAction.OnGenerate(MealPlanPreferences()))

        viewModel.onAction(IngredientsReceiptAction.OnCheckClick("Rice"))
        assertEquals(setOf("Rice"), viewModel.state.value.checkedIngredients)

        viewModel.onAction(IngredientsReceiptAction.OnCheckClick("Rice"))
        assertTrue(viewModel.state.value.checkedIngredients.isEmpty())
    }

    @Test
    fun `a checked ingredient drops off the receipt`() = runTest {
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()))
        viewModel.onAction(IngredientsReceiptAction.OnGenerate(MealPlanPreferences()))

        viewModel.onAction(IngredientsReceiptAction.OnCheckClick("Rice"))

        val state = viewModel.state.value
        val receiptNames = state.receiptCategories.flatMap { it.items }.map { it.name }
        assertFalse(receiptNames.contains("Rice"))
        assertEquals(3, receiptNames.size)
        assertEquals(3, state.receiptTotalItems)
        // Rice is only in PANTRY, so that category disappears entirely.
        assertFalse(state.receiptCategories.any { it.category == IngredientCategory.PANTRY })
        // The full list is untouched — the checklist still shows Rice, checked.
        assertEquals(4, state.ingredients.size)
        assertEquals(4, state.totalItems)
    }

    @Test
    fun `regenerating clears the checked ingredients`() = runTest {
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()))
        viewModel.onAction(IngredientsReceiptAction.OnGenerate(MealPlanPreferences()))
        viewModel.onAction(IngredientsReceiptAction.OnCheckClick("Rice"))

        viewModel.onAction(IngredientsReceiptAction.OnRetry)

        assertTrue(viewModel.state.value.checkedIngredients.isEmpty())
    }

    @Test
    fun `OnShareButtonClick flags a pending capture`() = runTest {
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()))
        viewModel.onAction(IngredientsReceiptAction.OnGenerate(MealPlanPreferences()))

        viewModel.onAction(IngredientsReceiptAction.OnShareButtonClick)

        assertTrue(viewModel.state.value.isSharing)
    }

    @Test
    fun `OnShareFinished clears the pending capture`() = runTest {
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()))
        viewModel.onAction(IngredientsReceiptAction.OnGenerate(MealPlanPreferences()))
        viewModel.onAction(IngredientsReceiptAction.OnShareButtonClick)

        viewModel.onAction(IngredientsReceiptAction.OnShareFinished)

        assertFalse(viewModel.state.value.isSharing)
    }

    @Test
    fun `tapping share twice does not restart the capture`() = runTest {
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()))
        viewModel.onAction(IngredientsReceiptAction.OnGenerate(MealPlanPreferences()))
        viewModel.onAction(IngredientsReceiptAction.OnShareButtonClick)
        val afterFirstTap = viewModel.state.value

        viewModel.onAction(IngredientsReceiptAction.OnShareButtonClick)

        // Same instance means no new state was emitted, so the capture composable is not restarted.
        assertSame(afterFirstTap, viewModel.state.value)
    }

    @Test
    fun `share does nothing while there is no receipt to render`() = runTest {
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()))

        // Still loading — OnGenerate has not run.
        viewModel.onAction(IngredientsReceiptAction.OnShareButtonClick)

        assertFalse(viewModel.state.value.isSharing)
    }

    @Test
    fun `OnContinueClick emits NavigateToMealPlan`() = runTest {
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()))

        viewModel.onAction(IngredientsReceiptAction.OnContinueClick)

        assertEquals(IngredientsReceiptEvent.NavigateToMealPlan, viewModel.events.first())
    }

    private fun viewModel(repository: RecipeRepository) = IngredientsReceiptViewModel(
        BuildGroceriesListUseCase(GenerateMealPlanUseCase(repository)),
    )

    private fun defaultCatalog(): List<Recipe> = listOf(
        Recipe(
            id = "r1",
            title = "Recipe 1",
            ingredients = listOf(
                Ingredient("Chicken"),
                Ingredient("Rice"),
                Ingredient("Spinach"),
                Ingredient("Hummus"),
            ),
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
