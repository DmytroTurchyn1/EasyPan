/*
 * Created  2/7/2026
 *
 * Copyright (c) 2026 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.home

import android.util.Log
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.repository.RecipeRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @RelaxedMockK
    private lateinit var recipeRepository: RecipeRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun testRecipe(id: String, chip: String) = Recipe(
        id = id,
        title = "Recipe $id",
        ingredients = emptyList(),
        preparationMinutes = 5,
        cookMinutes = 10,
        chips = listOf(chip),
        difficulty = "Easy",
        instructions = emptyList(),
        titleImg = ""
    )

    @Test
    fun `load failure exposes error state instead of crashing`() = runTest {
        coEvery { recipeRepository.getRecipes() } throws Exception("Firestore unavailable")
        val viewModel = HomeViewModel(recipeRepository)

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        assertEquals(AppError.UNKNOWN, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.recipes.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun `retry after failure loads recipes and clears error`() = runTest {
        coEvery { recipeRepository.getRecipes() } throws Exception("Firestore unavailable")
        val viewModel = HomeViewModel(recipeRepository)
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        assertEquals(AppError.UNKNOWN, viewModel.state.value.error)

        val recipe = testRecipe(id = "r1", chip = "Vegan")
        coEvery { recipeRepository.getRecipes() } returns listOf(recipe)
        viewModel.onAction(HomeAction.OnRetryClick)

        assertNull(viewModel.state.value.error)
        assertEquals(listOf(recipe), viewModel.state.value.recipes)
        assertEquals(listOf("Vegan"), viewModel.state.value.filterList)
        collectJob.cancel()
    }

    @Test
    fun `successful load populates recipes and filters`() = runTest {
        val recipes = listOf(
            testRecipe(id = "r1", chip = "Vegan"),
            testRecipe(id = "r2", chip = "Dessert")
        )
        coEvery { recipeRepository.getRecipes() } returns recipes
        val viewModel = HomeViewModel(recipeRepository)

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(recipes, viewModel.state.value.recipes)
        assertEquals(listOf("Dessert", "Vegan"), viewModel.state.value.filterList)
        collectJob.cancel()
    }

    @Test
    fun `filter selection narrows the recipe list and reselecting clears it`() = runTest {
        val vegan = testRecipe(id = "r1", chip = "Vegan")
        val dessert = testRecipe(id = "r2", chip = "Dessert")
        coEvery { recipeRepository.getRecipes() } returns listOf(vegan, dessert)
        val viewModel = HomeViewModel(recipeRepository)
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        viewModel.onAction(HomeAction.OnFilterSelected("Vegan"))
        assertEquals(listOf(vegan), viewModel.state.value.recipes)
        assertEquals("Vegan", viewModel.state.value.selectedFilter)

        viewModel.onAction(HomeAction.OnFilterSelected("Vegan"))
        assertEquals(listOf(vegan, dessert), viewModel.state.value.recipes)
        assertEquals("", viewModel.state.value.selectedFilter)
        collectJob.cancel()
    }
}
