package com.cook.easypan.easypan.presentation.ingredients_receipt

import com.cook.easypan.easypan.domain.model.MealPlanPreferences

sealed interface IngredientsReceiptAction {
    data class OnGenerate(val preferences: MealPlanPreferences) : IngredientsReceiptAction
    data class OnCheckClick(val ingredient: String) : IngredientsReceiptAction
    data object OnRetry : IngredientsReceiptAction
    data object OnShareButtonClick : IngredientsReceiptAction

    /** The off-screen render is over, whatever it produced. Sent before the file write. */
    data object OnShareFinished : IngredientsReceiptAction
    data object OnContinueClick : IngredientsReceiptAction
}
