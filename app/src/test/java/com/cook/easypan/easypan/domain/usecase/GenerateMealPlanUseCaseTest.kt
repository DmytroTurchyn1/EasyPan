package com.cook.easypan.easypan.domain.usecase

import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.repository.RecipeRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GenerateMealPlanUseCaseTest {

    private val monday = LocalDate.of(2026, 7, 13)

    @Test
    fun `excludes recipes that clash with allergies`() = runTest {
        val peanut = recipe("peanut", allergies = listOf("Peanuts"))
        val safe = recipe("safe", ingredients = listOf("Rice"))
        val useCase = GenerateMealPlanUseCase(FakeRecipeRepository(listOf(peanut, safe)))

        val plan = useCase(
            MealPlanPreferences(allergies = listOf("Peanuts"), mealsDay = 1),
            today = monday,
        )

        val used = plan.days.flatMap { it.meals }.map { it.id }.toSet()
        assertFalse("peanut" in used)
        assertTrue("safe" in used)
    }

    @Test
    fun `excludes disliked ingredients`() = runTest {
        val disliked = recipe("olives", ingredients = listOf("Olives"))
        val ok = recipe("plain", ingredients = listOf("Rice"))
        val useCase = GenerateMealPlanUseCase(FakeRecipeRepository(listOf(disliked, ok)))

        val plan = useCase(
            MealPlanPreferences(skipProducts = listOf("Olives"), mealsDay = 1),
            today = monday,
        )

        val used = plan.days.flatMap { it.meals }.map { it.id }.toSet()
        assertFalse("olives" in used)
        assertTrue("plain" in used)
    }

    @Test
    fun `ranks recipes with liked ingredients first`() = runTest {
        val plain = recipe("plain", ingredients = listOf("Rice"))
        val liked = recipe("liked", ingredients = listOf("Chicken", "Broccoli"))
        val useCase = GenerateMealPlanUseCase(FakeRecipeRepository(listOf(plain, liked)))

        val plan = useCase(
            MealPlanPreferences(favoriteProducts = listOf("Chicken", "Broccoli"), mealsDay = 1),
            today = monday,
        )

        assertEquals("liked", plan.days.first().meals.first().id)
    }

    @Test
    fun `builds seven days of the requested meal count`() = runTest {
        val catalog = (1..10).map { recipe("r$it", ingredients = listOf("Rice")) }
        val useCase = GenerateMealPlanUseCase(FakeRecipeRepository(catalog))

        val plan = useCase(MealPlanPreferences(mealsDay = 3), today = monday)

        assertEquals(7, plan.days.size)
        assertTrue(plan.days.all { it.meals.size == 3 })
        assertEquals(monday, plan.days.first().date)
        assertEquals(monday.plusDays(6), plan.days.last().date)
    }

    @Test
    fun `round-robins when catalog is smaller than the week`() = runTest {
        val catalog = listOf(
            recipe("a", ingredients = listOf("Rice")),
            recipe("b", ingredients = listOf("Rice")),
        )
        val useCase = GenerateMealPlanUseCase(FakeRecipeRepository(catalog))

        val plan = useCase(MealPlanPreferences(mealsDay = 1), today = monday)

        val firstMeals = plan.days.map { it.meals.first().id }
        assertEquals(firstMeals[0], firstMeals[2]) // cycles a, b, a, b...
        assertEquals(firstMeals[1], firstMeals[3])
    }

    @Test
    fun `falls back to the full catalog when filters exclude everything`() = runTest {
        val onlyPeanut = recipe("peanut", allergies = listOf("Peanuts"))
        val useCase = GenerateMealPlanUseCase(FakeRecipeRepository(listOf(onlyPeanut)))

        val plan = useCase(
            MealPlanPreferences(allergies = listOf("Peanuts"), mealsDay = 1),
            today = monday,
        )

        assertTrue(plan.days.isNotEmpty())
        assertEquals("peanut", plan.days.first().meals.first().id)
    }

    @Test
    fun `empty catalog yields no days`() = runTest {
        val useCase = GenerateMealPlanUseCase(FakeRecipeRepository(emptyList()))

        val plan = useCase(MealPlanPreferences(mealsDay = 3), today = monday)

        assertTrue(plan.days.isEmpty())
    }

    private fun recipe(
        id: String,
        ingredients: List<String> = emptyList(),
        allergies: List<String> = emptyList(),
    ) = Recipe(
        id = id,
        title = "Recipe $id",
        ingredients = ingredients,
        allergies = allergies,
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
