package com.cook.easypan.easypan.presentation.meal_plan_wizard

sealed interface MealPlanWizardEvent {
    data object Exit : MealPlanWizardEvent
    data object Finish : MealPlanWizardEvent
}
