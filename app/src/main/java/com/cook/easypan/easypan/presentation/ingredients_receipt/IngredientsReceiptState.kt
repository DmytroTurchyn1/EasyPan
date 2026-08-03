package com.cook.easypan.easypan.presentation.ingredients_receipt

import androidx.compose.runtime.Stable
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.easypan.domain.model.GroceryCategory
import com.cook.easypan.easypan.domain.model.GroceryItem

/**
 * @param ingredients every ingredient the plan needs, flattened for the checklist screen.
 * @param categories the same ingredients grouped — the source of truth for the receipt.
 * @param checkedIngredients names the user already has at home; excluded from the receipt.
 * @param isSharing true while the receipt is being rendered to an image for the share sheet.
 */
@Stable
data class IngredientsReceiptState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val mealsCount: Int = 0,
    val people: Int = 0,
    val totalItems: Int = 0,
    val ingredients: List<GroceryItem> = emptyList(),
    val categories: List<GroceryCategory> = emptyList(),
    val checkedIngredients: Set<String> = emptySet(),
    val isSharing: Boolean = false,
) {
    /** [categories] minus the checked ingredients, dropping any category left empty. */
    val receiptCategories: List<GroceryCategory>
        get() = if (checkedIngredients.isEmpty()) {
            categories
        } else {
            categories.mapNotNull { category ->
                val items = category.items.filterNot { it.name in checkedIngredients }
                if (items.isEmpty()) null else category.copy(items = items)
            }
        }

    /** Total shown on the receipt — only what the user still has to buy. */
    val receiptTotalItems: Int
        get() = totalItems - checkedIngredients.size
}
