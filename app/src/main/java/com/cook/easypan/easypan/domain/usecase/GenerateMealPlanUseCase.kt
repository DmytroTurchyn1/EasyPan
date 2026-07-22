package com.cook.easypan.easypan.domain.usecase

import com.cook.easypan.easypan.domain.model.MealPlan
import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import com.cook.easypan.easypan.domain.model.PlanDay
import com.cook.easypan.easypan.domain.repository.RecipeRepository
import java.time.LocalDate


class GenerateMealPlanUseCase(
    private val recipeRepository: RecipeRepository,
) {
    suspend operator fun invoke(
        preferences: MealPlanPreferences,
        today: LocalDate = LocalDate.now(),
    ): MealPlan {
        val catalog = recipeRepository.getRecipes()

        val allergies = preferences.allergies.mapToLowerSet()
        val skip = preferences.skipProducts.mapToLowerSet()
        val liked = preferences.favoriteProducts.mapToLowerSet()

        val ranked = catalog
            .filter { recipe ->
                recipe.allergies.none { it.lowercase() in allergies } &&
                        recipe.ingredients.none { it.lowercase() in skip }
            }
            .ifEmpty { catalog }
            .sortedByDescending { recipe ->
                recipe.ingredients.count { it.lowercase() in liked }
            }

        val mealsPerDay = preferences.mealsDay.coerceAtLeast(1)
        val days = if (ranked.isEmpty()) {
            emptyList()
        } else {
            (0 until DAYS_IN_PLAN).map { dayOffset ->
                PlanDay(
                    date = today.plusDays(dayOffset.toLong()),
                    meals = (0 until mealsPerDay).map { mealIndex ->
                        ranked[(dayOffset * mealsPerDay + mealIndex) % ranked.size]
                    },
                )
            }
        }

        return MealPlan(
            startDate = today,
            days = days,
            preferences = preferences,
        )
    }

    private fun List<String>.mapToLowerSet(): Set<String> = mapTo(mutableSetOf()) { it.lowercase() }

    private companion object {
        const val DAYS_IN_PLAN = 7
    }
}
