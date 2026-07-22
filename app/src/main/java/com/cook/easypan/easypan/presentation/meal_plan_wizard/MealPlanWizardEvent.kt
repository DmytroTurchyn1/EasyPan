package com.cook.easypan.easypan.presentation.meal_plan_wizard

import com.cook.easypan.easypan.domain.model.MealPlanPreferences

sealed interface MealPlanWizardEvent {
    data object Exit : MealPlanWizardEvent
    data class Finish(val preferences: MealPlanPreferences) : MealPlanWizardEvent
}
