package com.cook.easypan.easypan.presentation.ingredients_receipt

sealed interface IngredientsReceiptEvent {
    data object NavigateToMealPlan : IngredientsReceiptEvent
}
