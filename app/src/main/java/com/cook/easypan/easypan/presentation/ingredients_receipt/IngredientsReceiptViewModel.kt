package com.cook.easypan.easypan.presentation.ingredients_receipt

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import com.cook.easypan.easypan.domain.usecase.BuildGroceriesListUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IngredientsReceiptViewModel(
    private val buildGroceriesList: BuildGroceriesListUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(IngredientsReceiptState())
    val state = _state.asStateFlow()

    // Kept so Retry can rebuild the same list without re-plumbing the preferences.
    private var lastPreferences: MealPlanPreferences? = null

    fun onAction(action: IngredientsReceiptAction) {
        when (action) {
            is IngredientsReceiptAction.OnGenerate -> generate(action.preferences)
            IngredientsReceiptAction.OnRetry -> lastPreferences?.let { generate(it) }

            // Buttons are wired to navigation/share later.
            IngredientsReceiptAction.OnShareButtonClick -> Unit
            IngredientsReceiptAction.OnEditClick -> Unit
        }
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
