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
 * @param quantity the total the plan calls for, summed across every meal slot (e.g. "2 1/2 cups").
 * Amounts in units that cannot be combined stay joined (e.g. "2 cups + 100 g"), and it is null when
 * no recipe specified an amount.
 */
data class GroceryItem(
    val name: String,
    val quantity: String? = null,
)
