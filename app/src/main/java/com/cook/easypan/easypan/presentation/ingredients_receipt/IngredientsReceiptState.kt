package com.cook.easypan.easypan.presentation.ingredients_receipt

import androidx.compose.runtime.Stable
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.easypan.domain.model.GroceryCategory

@Stable
data class IngredientsReceiptState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val mealsCount: Int = 0,
    val people: Int = 0,
    val totalItems: Int = 0,
    val categories: List<GroceryCategory> = emptyList(),
)
