package com.cook.easypan.easypan.presentation.meal_plan_wizard

sealed interface MealPlanWizardAction {
    // Step 1
    data class OnSearchQueryChange(val query: String) : MealPlanWizardAction
    data class OnIngredientToggle(val ingredient: String) : MealPlanWizardAction
    data class OnIngredientSkipToggle(val ingredient: String) : MealPlanWizardAction
    data class OnAllergyCheck(val allergy: String) : MealPlanWizardAction

    // Step 2
    data class OnPortionsSelect(val portions: Int) : MealPlanWizardAction

    // Step 5
    data class OnMealsSelect(val meals: Int) : MealPlanWizardAction

    // Navigation
    data object OnContinueClick : MealPlanWizardAction
    data object OnSkipClick : MealPlanWizardAction
    data object OnBackClick : MealPlanWizardAction
    data object OnCancelClick : MealPlanWizardAction
}
