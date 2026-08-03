package com.cook.easypan.easypan.domain.usecase

import com.cook.easypan.easypan.domain.model.Ingredient
import com.cook.easypan.easypan.domain.model.IngredientCategory
import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.repository.RecipeRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BuildGroceriesListUseCaseTest {

    private val monday = LocalDate.of(2026, 7, 20)

    @Test
    fun `dedupes ingredients repeated across the round-robin week`() = runTest {
        // Two recipes cycled over 21 slots; "Rice" appears in both with different casing.
        val useCase = useCase(
            recipe("a", ingredients = listOf("Rice", "Chicken")),
            recipe("b", ingredients = listOf("rice", "Spinach")),
        )

        val groceries = useCase(MealPlanPreferences(mealsDay = 3, people = 2), today = monday)

        val allItems = groceries.categories.flatMap { it.items }
        assertEquals(3, groceries.totalItems)
        assertEquals(listOf("Chicken", "Rice", "Spinach"), allItems.map { it.name }.sorted())
        // First-seen casing is kept.
        assertTrue(allItems.any { it.name == "Rice" })
    }

    @Test
    fun `joins every quantity recorded for the same ingredient`() = runTest {
        val useCase = useCase(
            recipeOf("a", Ingredient("Milk", "1/2 cup"), Ingredient("Salt")),
            recipeOf("b", Ingredient("milk", "2 cups")),
        )

        val groceries = useCase(MealPlanPreferences(mealsDay = 3), today = monday)

        val items = groceries.categories.flatMap { it.items }.associateBy { it.name }
        assertEquals(2, groceries.totalItems)
        assertEquals("1/2 cup + 2 cups", items.getValue("Milk").quantity)
        // Nothing to show when no recipe gave an amount.
        assertNull(items.getValue("Salt").quantity)
    }

    @Test
    fun `does not repeat an identical quantity`() = runTest {
        val useCase = useCase(
            recipeOf("a", Ingredient("Rice", "200 g")),
            recipeOf("b", Ingredient("rice", "200 g")),
        )

        val groceries = useCase(MealPlanPreferences(mealsDay = 3), today = monday)

        val rice = groceries.categories.flatMap { it.items }.single { it.name == "Rice" }
        assertEquals("200 g", rice.quantity)
    }

    @Test
    fun `groups by category in enum order and drops empty categories`() = runTest {
        val useCase = useCase(
            recipe("a", ingredients = listOf("Hummus", "Rice", "Chicken", "Spinach")),
        )

        val groceries = useCase(MealPlanPreferences(), today = monday)

        assertEquals(
            listOf(
                IngredientCategory.PRODUCE,
                IngredientCategory.MEAT_FISH,
                IngredientCategory.PANTRY,
                IngredientCategory.OTHER,
            ),
            groceries.categories.map { it.category },
        )
    }

    @Test
    fun `items are sorted alphabetically within a category`() = runTest {
        val useCase = useCase(
            recipe("a", ingredients = listOf("Tomatoes", "Broccoli", "Spinach")),
        )

        val groceries = useCase(MealPlanPreferences(), today = monday)

        val produce = groceries.categories.single { it.category == IngredientCategory.PRODUCE }
        assertEquals(listOf("Broccoli", "Spinach", "Tomatoes"), produce.items.map { it.name })
    }

    @Test
    fun `carries meals count and people from the plan`() = runTest {
        val useCase = useCase(recipe("a", ingredients = listOf("Rice")))

        val groceries = useCase(MealPlanPreferences(mealsDay = 3, people = 2), today = monday)

        assertEquals(21, groceries.mealsCount) // 7 days × 3 meals
        assertEquals(2, groceries.people)
    }

    @Test
    fun `empty catalog yields an empty list`() = runTest {
        val useCase = BuildGroceriesListUseCase(
            GenerateMealPlanUseCase(FakeRecipeRepository(emptyList())),
        )

        val groceries = useCase(MealPlanPreferences(mealsDay = 3), today = monday)

        assertTrue(groceries.categories.isEmpty())
        assertEquals(0, groceries.totalItems)
        assertEquals(0, groceries.mealsCount)
    }

    private fun useCase(vararg recipes: Recipe) = BuildGroceriesListUseCase(
        GenerateMealPlanUseCase(FakeRecipeRepository(recipes.toList())),
    )

    private fun recipe(
        id: String,
        ingredients: List<String>,
    ) = recipeOf(id, *ingredients.map { Ingredient(it) }.toTypedArray())

    private fun recipeOf(
        id: String,
        vararg ingredients: Ingredient,
    ) = Recipe(
        id = id,
        title = "Recipe $id",
        ingredients = ingredients.toList(),
        allergies = emptyList(),
        preparationMinutes = 10,
        cookMinutes = 20,
        chips = emptyList(),
        difficulty = "Easy",
        instructions = emptyList(),
        titleImg = "",
    )

    private class FakeRecipeRepository(
        private val recipes: List<Recipe>,
    ) : RecipeRepository {
        override suspend fun getRecipes(): List<Recipe> = recipes
    }
}
