package com.cook.easypan.easypan.presentation.ingredients_receipt

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import com.cook.easypan.easypan.domain.usecase.BuildGroceriesListUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IngredientsReceiptViewModel(
    private val buildGroceriesList: BuildGroceriesListUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(IngredientsReceiptState())
    val state = _state.asStateFlow()

    private val _events = Channel<IngredientsReceiptEvent>()
    val events = _events.receiveAsFlow()

    // Kept so Retry can rebuild the same list without re-plumbing the preferences.
    private var lastPreferences: MealPlanPreferences? = null

    fun onAction(action: IngredientsReceiptAction) {
        when (action) {
            is IngredientsReceiptAction.OnGenerate -> generate(action.preferences)
            IngredientsReceiptAction.OnRetry -> lastPreferences?.let { generate(it) }
            IngredientsReceiptAction.OnContinueClick ->
                sendEvent(IngredientsReceiptEvent.NavigateToMealPlan)

            // Rendering the receipt to an image happens in the UI layer; this only flags that a
            // capture is pending. Ignored when a render is already running, so a double tap cannot
            // start two of them, and when there is no receipt to render yet.
            IngredientsReceiptAction.OnShareButtonClick -> _state.update { state ->
                if (state.isSharing || state.isLoading || state.error != null) {
                    state
                } else {
                    state.copy(isSharing = true)
                }
            }

            IngredientsReceiptAction.OnShareFinished -> _state.update { it.copy(isSharing = false) }

            // Checked means "I already have this" — it drops off the receipt, but stays
            // on the checklist so it can be unchecked again.
            is IngredientsReceiptAction.OnCheckClick -> _state.update { state ->
                state.copy(
                    checkedIngredients = if (action.ingredient in state.checkedIngredients) {
                        state.checkedIngredients - action.ingredient
                    } else {
                        state.checkedIngredients + action.ingredient
                    }
                )
            }
        }
    }

    private fun sendEvent(event: IngredientsReceiptEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private fun generate(preferences: MealPlanPreferences) {
        lastPreferences = preferences
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val groceries = buildGroceriesList(preferences)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        mealsCount = groceries.mealsCount,
                        people = groceries.people,
                        totalItems = groceries.totalItems,
                        categories = groceries.categories,
                        ingredients = groceries.categories.flatMap { category -> category.items },
                        checkedIngredients = emptySet(),
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to build groceries list", e)
                _state.update { it.copy(isLoading = false, error = AppError.UNKNOWN) }
            }
        }
    }

    private companion object {
        const val TAG = "IngredientsReceiptVM"
    }
}
