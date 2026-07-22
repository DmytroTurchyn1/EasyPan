package com.cook.easypan.easypan.presentation.meal_plan_review

import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.repository.RecipeRepository
import com.cook.easypan.easypan.domain.usecase.GenerateMealPlanUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MealPlanReviewViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `OnGenerate populates the plan`() = runTest {
        val viewModel = viewModel()
        val stateJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        viewModel.onAction(
            MealPlanReviewAction.OnGenerate(
                MealPlanPreferences(people = 2, mealsDay = 3, allergies = listOf("Peanuts")),
            ),
        )

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(7, state.days.size)
        assertTrue(state.days.all { it.meals.size == 3 })
        assertEquals(2, state.people)
        assertEquals(3, state.mealsDay)
        assertTrue(state.weekRange.isNotEmpty())
        stateJob.cancel()
    }

    @Test
    fun `recipe click emits OpenRecipe`() = runTest {
        val viewModel = viewModel()
        val events = mutableListOf<MealPlanReviewEvent>()
        val eventsJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }

        val recipe = defaultCatalog().first()
        viewModel.onAction(MealPlanReviewAction.OnRecipeClick(recipe))

        assertEquals(listOf<MealPlanReviewEvent>(MealPlanReviewEvent.OpenRecipe(recipe)), events)
        eventsJob.cancel()
    }

    @Test
    fun `continue and dismiss emit Dismiss, edit emits EditPlan`() = runTest {
        val viewModel = viewModel()
        val events = mutableListOf<MealPlanReviewEvent>()
        val eventsJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onAction(MealPlanReviewAction.OnContinueClick)
        viewModel.onAction(MealPlanReviewAction.OnDismissPlanClick)
        viewModel.onAction(MealPlanReviewAction.OnEditClick)

        assertEquals(
            listOf(
                MealPlanReviewEvent.Dismiss,
                MealPlanReviewEvent.Dismiss,
                MealPlanReviewEvent.EditPlan,
            ),
            events,
        )
        eventsJob.cancel()
    }

    private fun viewModel(recipes: List<Recipe> = defaultCatalog()) =
        MealPlanReviewViewModel(GenerateMealPlanUseCase(FakeRecipeRepository(recipes)))

    private fun defaultCatalog(): List<Recipe> = (1..8).map { index ->
        Recipe(
            id = "r$index",
            title = "Recipe $index",
            ingredients = listOf("Rice"),
            allergies = emptyList(),
            preparationMinutes = 10,
            cookMinutes = 20,
            chips = emptyList(),
            difficulty = "Easy",
            instructions = emptyList(),
            titleImg = "",
        )
    }

    private class FakeRecipeRepository(
        private val recipes: List<Recipe>,
    ) : RecipeRepository {
        override suspend fun getRecipes(): List<Recipe> = recipes
    }
}
