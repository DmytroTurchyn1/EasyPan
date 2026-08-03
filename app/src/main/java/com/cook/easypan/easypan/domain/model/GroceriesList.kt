package com.cook.easypan.easypan.domain.model

/**
 * The aggregated shopping list for a generated weekly meal plan,
 * shown on the Groceries Receipt screen.
 *
 * @param categories non-empty categories in [IngredientCategory] order.
 * @param totalItems number of unique ingredients across the whole plan.
 * @param mealsCount number of meal slots in the plan (days × meals per day).
 * @param people household size the plan was generated for.
 */
data class GroceriesList(
    val categories: List<GroceryCategory>,
    val totalItems: Int,
    val mealsCount: Int,
    val people: Int,
)

data class GroceryCategory(
    val category: IngredientCategory,
    val items: List<GroceryItem>,
)

/**
 * One line on the shopping list.
 *
 * @param name the ingredient, deduped across the whole plan.
 * @param quantity every amount the plan calls for, joined (e.g. "1/2 cup + 2 cups"),
 * or null when no recipe specified one.
 */
data class GroceryItem(
    val name: String,
    val quantity: String? = null,
)
