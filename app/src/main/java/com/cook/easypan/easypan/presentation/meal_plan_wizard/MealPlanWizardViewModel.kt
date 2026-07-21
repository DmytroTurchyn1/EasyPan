package com.cook.easypan.easypan.presentation.meal_plan_wizard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MealPlanWizardViewModel : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(MealPlanWizardState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                _state.update {
                    it.copy(
                        suggestions = INGREDIENT_CATALOG,
                        allergies = ALLERGIES_CATALOG
                    )
                }
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MealPlanWizardState()
        )

    private val _events = Channel<MealPlanWizardEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: MealPlanWizardAction) {
        when (action) {
            is MealPlanWizardAction.OnSearchQueryChange -> {
                _state.update { it.copy(searchQuery = action.query) }
            }

            is MealPlanWizardAction.OnIngredientToggle -> {
                _state.update { current ->
                    val updated = if (action.ingredient in current.selectedIngredients) {
                        current.selectedIngredients - action.ingredient
                    } else {
                        current.selectedIngredients + action.ingredient
                    }
                    current.copy(selectedIngredients = updated)
                }
            }

            is MealPlanWizardAction.OnIngredientSkipToggle -> {
                _state.update { current ->
                    val updated = if (action.ingredient in current.selectedIngredientsSkip) {
                        current.selectedIngredientsSkip - action.ingredient
                    } else {
                        current.selectedIngredientsSkip + action.ingredient
                    }
                    current.copy(selectedIngredientsSkip = updated)
                }
            }

            is MealPlanWizardAction.OnPortionsSelect -> {
                _state.update { it.copy(portions = action.portions) }
            }

            is MealPlanWizardAction.OnMealsSelect -> {
                _state.update { it.copy(selectedMeals = action.meals) }
            }

            is MealPlanWizardAction.OnAllergyCheck -> {
                _state.update { current ->
                    val updated = if (action.allergy in current.selectedAllergies) {
                        current.selectedAllergies - action.allergy
                    } else {
                        current.selectedAllergies + action.allergy
                    }
                    current.copy(selectedAllergies = updated)
                }
            }

            MealPlanWizardAction.OnContinueClick -> {
                if (_state.value.isLastStep) {
                    sendEvent(MealPlanWizardEvent.Finish)
                } else {
                    _state.update { it.copy(step = it.step + 1) }
                }
            }

            MealPlanWizardAction.OnSkipClick -> {
                // Skip records no allergies and moves on.
                if (_state.value.isLastStep) {
                    sendEvent(MealPlanWizardEvent.Finish)
                } else {
                    _state.update {
                        it.copy(step = it.step + 1, selectedAllergies = emptyList())
                    }
                }
            }

            MealPlanWizardAction.OnBackClick -> {
                if (_state.value.step > 1) {
                    _state.update { it.copy(step = it.step - 1) }
                } else {
                    sendEvent(MealPlanWizardEvent.Exit)
                }
            }

            MealPlanWizardAction.OnCancelClick -> sendEvent(MealPlanWizardEvent.Exit)
        }
    }

    private fun sendEvent(event: MealPlanWizardEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private companion object {
        val INGREDIENT_CATALOG = listOf(
            "Quinoa",
            "Salad",
            "Stir-fry",
            "Hummus",
            "Smoothie",
            "Avocado",
            "Chicken",
            "Salmon",
            "Rice",
            "Pasta",
            "Eggs",
            "Tofu",
            "Beans",
            "Broccoli",
            "Sweet potato",
        )

        val ALLERGIES_CATALOG = listOf(
            "Peanuts",
            "Tree Nuts",
            "Dairy",
            "Eggs",
            "Gluten",
            "Soy",
            "Sesame",
        )
    }

}
