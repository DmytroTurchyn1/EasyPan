package com.cook.easypan.easypan.domain.usecase

import com.cook.easypan.easypan.domain.model.GroceriesList
import com.cook.easypan.easypan.domain.model.GroceryCategory
import com.cook.easypan.easypan.domain.model.GroceryItem
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
 * Amounts for the same ingredient are summed by [QuantityMerger]. Every meal slot counts, so a
 * recipe that fills three slots contributes its ingredients three times — the list has to cover a
 * whole week of cooking.
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

        // Unique ingredients across the whole week — first-seen casing wins, and every
        // amount the plan calls for is collected under that one name to be summed.
        val namesByKey = LinkedHashMap<String, String>()
        val quantitiesByKey = LinkedHashMap<String, MutableList<String>>()
        slots.forEach { recipe ->
            recipe.ingredients.forEach { ingredient ->
                val name = ingredient.name.trim()
                if (name.isNotEmpty()) {
                    val key = name.lowercase()
                    namesByKey.putIfAbsent(key, name)
                    val quantity = ingredient.quantity.trim()
                    if (quantity.isNotEmpty()) {
                        quantitiesByKey.getOrPut(key) { mutableListOf() }.add(quantity)
                    }
                }
            }
        }

        val grouped = namesByKey.entries
            .sortedBy { (key, _) -> key }
            .map { (key, name) ->
                GroceryItem(
                    name = name,
                    quantity = quantitiesByKey[key]?.let(QuantityMerger::merge)
                )
            }
            .groupBy { IngredientCategorizer.categorize(it.name) }

        return GroceriesList(
            categories = IngredientCategory.entries.mapNotNull { category ->
                grouped[category]?.let { items -> GroceryCategory(category, items) }
            },
            totalItems = namesByKey.size,
            mealsCount = slots.size,
            people = preferences.people,
        )
    }
}
