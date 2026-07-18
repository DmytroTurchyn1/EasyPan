package com.cook.easypan.easypan.presentation.meal_plan

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MealPlanViewModel : ViewModel() {

    private val _state = MutableStateFlow(MealPlanState())
    val state = _state.asStateFlow()

    fun onAction(action: MealPlanAction) {
        when (action) {
            MealPlanAction.OnCreatePlanClick -> Unit // Navigation handled in MealPlanRoot
        }
    }
}
