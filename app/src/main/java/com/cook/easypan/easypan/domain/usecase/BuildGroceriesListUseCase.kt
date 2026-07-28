package com.cook.easypan.easypan.domain.usecase

import com.cook.easypan.easypan.domain.model.GroceriesList
import com.cook.easypan.easypan.domain.model.GroceryCategory
import com.cook.easypan.easypan.domain.model.IngredientCategory
import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import java.time.LocalDate

/**
 * Builds the weekly shopping list for the Groceries Receipt screen:
 * regenerates the deterministic [com.cook.easypan.easypan.domain.model.MealPlan] from the wizard
 * [MealPlanPreferences] (same plan the review screen shows), flattens every meal slot's
 * ingredients, dedupes them case-insensitively, and groups them by [IngredientCategory]
 * via [IngredientCategorizer].
 *
 * [today] is injectable so the result is deterministic under test.
 */
class BuildGroceriesListUseCase(
    private val generateMealPlan: GenerateMealPlanUseCase,
) {
    suspend operator fun invoke(
        preferences: MealPlanPreferences,
        today: LocalDate = LocalDate.now(),
    ): GroceriesList {
        val plan = generateMealPlan(preferences, today)
        val slots = plan.days.flatMap { it.meals }

        // Unique ingredients across the whole week — first-seen casing wins.
        val uniqueByKey = LinkedHashMap<String, String>()
        slots.forEach { recipe ->
            recipe.ingredients.forEach { ingredient ->
                val trimmed = ingredient.trim()
                if (trimmed.isNotEmpty()) {
                    uniqueByKey.putIfAbsent(trimmed.lowercase(), trimmed)
                }
            }
        }

        val grouped = uniqueByKey.values
            .sortedBy { it.lowercase() }
            .groupBy { IngredientCategorizer.categorize(it) }

        return GroceriesList(
            categories = IngredientCategory.entries.mapNotNull { category ->
                grouped[category]?.let { items -> GroceryCategory(category, items) }
            },
            totalItems = uniqueByKey.size,
            mealsCount = slots.size,
            people = preferences.people,
        )
    }
}
