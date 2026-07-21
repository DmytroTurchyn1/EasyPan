package com.cook.easypan.easypan.presentation.meal_plan_wizard

import androidx.compose.runtime.Stable

@Stable
data class MealPlanWizardState(
    val step: Int = 1,
    val steps: Int = 5,
    // Step 1 — ingredients
    val searchQuery: String = "",
    val suggestions: List<String> = emptyList(),
    val selectedIngredients: Set<String> = emptySet(),
    val selectedIngredientsSkip: Set<String> = emptySet(),
    // Step 2 — portions
    val portions: Int? = null,
    // Step 3 — allergies
    val allergies: List<String> = emptyList(),
    val selectedAllergies: List<String> = emptyList(),
    // Step 5 — meals a day
    val selectedMeals: Int = 3
) {
    val progressBar: Float get() = step.toFloat() / steps

    val isLastStep: Boolean get() = step >= steps

    val canContinue: Boolean
        get() = when (step) {
            2 -> portions != null
            else -> true
        }
}
