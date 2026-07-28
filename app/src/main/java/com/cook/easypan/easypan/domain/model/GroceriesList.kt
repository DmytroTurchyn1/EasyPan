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
    val items: List<String>,
)
